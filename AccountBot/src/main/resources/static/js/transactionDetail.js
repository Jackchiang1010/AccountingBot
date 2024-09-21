let type;

document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const lineUserId = urlParams.get('lineUserId');
    const transactionId = urlParams.get('transactionId');

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

                type = data.type;
                category = data.category;
                return fetch(`/api/1.0/category/get?type=${type}&name=all&lineUserId=${lineUserId}`);
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

    document.getElementById('updateForm').addEventListener('submit', confirmUpdate);

});

function confirmUpdate(event) {
    event.preventDefault();

    if (confirm("確定要更新這筆記錄嗎？")) {
        const urlParams = new URLSearchParams(window.location.search);
        const transactionId = urlParams.get('transactionId');

        // 獲取表單中的資料
        const date = document.getElementById('date').value;
        const cost = document.getElementById('amount').value;
        const category = document.getElementById('category').value;
        const description = document.getElementById('note').value;
        const lineUserId = urlParams.get('lineUserId');

        // 構建要發送的資料物件
        const data = {
            id: transactionId,
            type: type,
            category: category,
            cost: cost,
            description: description,
            date: date,
            lineUserId: lineUserId
        };

        // 呼叫更新 API
        fetch('/api/1.0/transaction/update', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data) // 將資料轉換為 JSON 格式
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('更新失敗');
                }
                return response.json();
            })
            .then(data => {
                console.log('API 回應資料:', data);
                alert("記錄已更新");
                window.location.href = `dashboard.html`;
            })
            .catch(error => {
                console.error('更新資料失敗:', error);
            });
    }
}

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

