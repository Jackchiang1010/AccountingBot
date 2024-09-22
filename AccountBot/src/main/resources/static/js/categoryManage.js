let type;
let selectedCategory;

// 在網頁加載時執行
document.addEventListener('DOMContentLoaded', function() {
    const checkLineUserId = setInterval(() => {
        if (lineUserId) {
            clearInterval(checkLineUserId); // 清除檢查
            console.log("lineUserId : " + lineUserId);

            // 呼叫 fetchCategories 並傳入 type 和 lineUserId
            fetchCategories();
        }
    }, 100); // 每100毫秒檢查一次

    const urlParams = new URLSearchParams(window.location.search);
    type = urlParams.get('type');
});

// 定義取得分類資料的函數
function fetchCategories() {
    const categoryList = document.getElementById("category-list");
    const budgetSection = document.querySelector('.budgets');

    // 檢查 type 是否為 0，決定是否顯示預算區塊
    if (type == 0) {
        budgetSection.style.display = 'none';
        categoryList.style.textAlign = 'center';
    } else {
        budgetSection.style.display = 'block';
        categoryList.style.textAlign = 'left';
    }

    const apiUrl = `/api/1.0/category/get?type=${type}&name=all&lineUserId=${lineUserId}`;

    // 清空原有的分類列表
    categoryList.innerHTML = "";

    fetch(apiUrl)
        .then(response => response.json())
        .then(data => {
            if (data && data.data) {
                data.data.forEach(category => {
                    const li = document.createElement("li");
                    li.textContent = category.name;
                    li.classList.add("selectable"); // 添加 selectable 類別
                    li.addEventListener("click", function() {
                        // 取消之前選中的分類
                        if (selectedCategory) {
                            selectedCategory.classList.remove("selected");
                        }
                        // 設定新的選中分類
                        selectedCategory = li;
                        selectedCategory.classList.add("selected"); // 標記為選中
                        console.log("選中的分類: ", selectedCategory.textContent); // 紀錄被選中的分類
                    });
                    categoryList.appendChild(li);
                });
                // 取得預算資料
                fetchBudgets(data.data.map(c => c.name));
            } else {
                console.error("無法取得分類資料");
            }
        })
        .catch(error => {
            console.error("發生錯誤: ", error);
        });
}

// 定義取得預算資料的函數
function fetchBudgets(categories) {
    const budgetList = document.getElementById("budget-list");

    const apiUrl = `/api/1.0/budget/get?category=all&lineUserId=${lineUserId}`;

    fetch(apiUrl)
        .then(response => response.json())
        .then(data => {
            const budgets = {};

            if (data && data.data) {
                data.data.forEach(budget => {
                    budgets[budget.category] = budget.price;
                });
            } else {
                console.error("無法取得預算資料");
            }

            budgetList.innerHTML = ''; // 清空原有預算列表

            // 將所有分類和對應預算加入到 budget-list 中
            categories.forEach(category => {
                const price = budgets[category] !== undefined ? `${budgets[category]} 元` : '未設立預算';
                const li = document.createElement("li");
                li.textContent = `${category}: ${price}`;
                budgetList.appendChild(li);
            });
        })
        .catch(error => {
            console.error("發生錯誤: ", error);
        });
}
