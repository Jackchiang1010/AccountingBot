let type = 1;
let selectedCategory = "";

// 在網頁加載時執行
document.addEventListener('DOMContentLoaded', function() {
    const checkLineUserId = setInterval(() => {
        if (lineUserId) {
            clearInterval(checkLineUserId); // 清除檢查
            console.log("lineUserId : " + lineUserId);
            updateCategories();
        }
    }, 100); // 每100毫秒檢查一次
});

document.addEventListener('DOMContentLoaded', function() {
    const amountInput = document.getElementById('amount');
    const numpadButtons = document.querySelectorAll('.numpad button');
    const expenseButton = document.getElementById('expense');
    const incomeButton = document.getElementById('income');
    const dateInput = document.getElementById('date');
    const eraseButton = document.getElementById('erase');
    let currentMode = 'expense';

    const urlParams = new URLSearchParams(window.location.search);
    const dateParam = urlParams.get('date');

    console.log("dateParam : " + dateParam);

    // 設定預設日期為當天
    const today = new Date();
    const formattedDate = dateParam ? dateParam : today.toISOString().split('T')[0];
    dateInput.value = formattedDate;

    numpadButtons.forEach(button => {
        button.addEventListener('click', function() {
            const value = this.textContent;
            if (value === 'AC') {
                amountInput.value = '';
            } else if (value === '=') {
                try {
                    amountInput.value = eval(amountInput.value);
                } catch (error) {
                    amountInput.value = 'Error';
                }
            } else if (this.id === 'erase') {
                amountInput.value = amountInput.value.slice(0, -1); // 每次移除最後一個字元
            } else {
                amountInput.value += value;
            }
        });
    });

    // 監聽鍵盤事件，當按下 Enter 鍵時模擬按下 '=' 按鈕
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Enter') {
            try {
                amountInput.value = eval(amountInput.value);
            } catch (error) {
                amountInput.value = 'Error';
            }
        }
    });

    expenseButton.addEventListener('click', function() {
        currentMode = 'expense';
        type = 1;
        console.log("type : " + type);
        updateCategories();
        this.style.backgroundColor = '#ff9999';
        incomeButton.style.backgroundColor = '';
    });

    incomeButton.addEventListener('click', function() {
        currentMode = 'income';
        type = 0;
        console.log("type : " + type);
        updateCategories();
        this.style.backgroundColor = '#99ff99';
        expenseButton.style.backgroundColor = '';
    });

    document.getElementById('save').addEventListener('click', function() {
        const cost = amountInput.value;
        const description = document.querySelector('.description textarea').value;
        const date = dateInput.value;

        if (!selectedCategory) {
            alert('請選擇一個類別！');
            return;
        }

        console.log("type : " + type);
        console.log("category : " + selectedCategory);
        console.log("cost : " + cost);
        console.log("description : " + description);
        console.log("date : " + date);
        console.log("lineUserId : " + lineUserId);

        const recordData = {
            type: type,
            category: selectedCategory,
            cost: cost,
            description: description,
            date: date,
            lineUserId: lineUserId
        };

        fetch('/api/1.0/transaction/record', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(recordData)
        })
            .then(response => {
                if (response.ok) {
                    alert('已儲存記錄！類型: ' + (type === 1 ? '支出' : '收入'));
                    window.location.href="/record.html";
                } else {
                    alert('儲存失敗！請稍後再試。');
                }
            })
            .catch(error => {
                console.error('Error saving record:', error);
            });
    });

});

// 更新類別按鈕
function updateCategories() {
    const categoriesDiv = document.querySelector('.categories');
    const apiUrl = `/api/1.0/category/get?type=${type}&name=all&lineUserId=${lineUserId}`;

    fetch(apiUrl)
        .then(response => response.json())
        .then(data => {
            categoriesDiv.innerHTML = ''; // 清空現有的類別按鈕

            // 根據回傳的資料動態生成按鈕
            data.data.forEach(category => {
                const button = document.createElement('button');
                button.textContent = category.name;

                // 為每個按鈕添加點擊事件
                button.addEventListener('click', function() {
                    selectedCategory = category.name; // 設定選擇的分類
                    // 移除其他按鈕的選中樣式
                    document.querySelectorAll('.categories button').forEach(btn => {
                        btn.classList.remove('selected');
                        btn.style.backgroundColor = ''; // 重置背景顏色
                    });
                    // 添加選中樣式
                    button.classList.add('selected');
                    button.style.backgroundColor = '#FFF2CC'; // 選中顏色
                    console.log("Selected category: " + selectedCategory);
                });

                categoriesDiv.appendChild(button);
            });

            // 固定「分類管理」按鈕
            const manageButton = document.createElement('button');
            manageButton.textContent = '分類管理';
            manageButton.classList.add('manage-button');

            // 設定點擊事件，跳轉到分類管理頁面
            manageButton.onclick = function() {
                window.location.href = `/categoryManage.html?type=${type}`;
            };

            categoriesDiv.appendChild(manageButton);
        })
        .catch(error => {
            console.error('Error fetching categories:', error);
        });
}
