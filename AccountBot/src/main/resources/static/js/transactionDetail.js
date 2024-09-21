document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const lineUserId = urlParams.get('lineUserId');
    const transactionId = urlParams.get('transactionId');

    let transactionType;
    let category;

    if (transactionId) {
        fetch(`/api/1.0/transaction/get/byId?id=${transactionId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('網路錯誤');
                }
                return response.json();
            })
            .then(data => {
                // 設置預設值
                document.getElementById('date').value = data.date;
                document.getElementById('amount').value = data.cost;
                document.getElementById('note').value = data.description;

                transactionType = data.type;
                category = data.category;
                return fetch(`/api/1.0/category/get?type=${transactionType}&name=all&lineUserId=${lineUserId}`);
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('網路錯誤');
                }
                return response.json();
            })
            .then(data => {
                const categories = data.data || []; // 確保 categories 是陣列
                const categorySelect = document.getElementById('category');

                categories.forEach(category => {
                    const option = document.createElement('option');
                    option.value = category.name;
                    option.textContent = category.name; // 顯示名稱
                    categorySelect.appendChild(option);
                });

                // 設置下拉選單的預設值
                categorySelect.value = category;
            })
            .catch(error => {
                console.error('取得資料失敗:', error);
            });
    }
});

function confirmDelete() {
    if (confirm("確定要刪除這筆記錄嗎？")) {
        const urlParams = new URLSearchParams(window.location.search);
        const transactionId = urlParams.get('transactionId');

        if (transactionId) {
            fetch(`/api/1.0/transaction/delete`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ id: transactionId }) // 將要刪除的 ID 傳遞給 API
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('刪除失敗');
                    }
                    return response.json();
                })
                .then(data => {
                    alert("記錄已刪除");
                    window.location.href = `dashboard.html`;
                })
                .catch(error => {
                    console.error('刪除資料失敗:', error);
                });
        } else {
            alert("無法獲取交易 ID");
        }
    }
}

