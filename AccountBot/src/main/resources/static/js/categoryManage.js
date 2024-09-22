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
                    li.dataset.id = category.id; // 假設 API 返回的 category 對象中有 id 屬性
                    li.classList.add("selectable");
                    addSelectableListener(li); // 添加選擇事件
                    categoryList.appendChild(li);
                });

                // 添加 "新增分類" 選項
                const addCategoryLi = document.createElement("li");
                addCategoryLi.textContent = "新增分類";
                addCategoryLi.classList.add("selectable");
                addSelectableListener(addCategoryLi); // 添加選擇事件
                categoryList.appendChild(addCategoryLi);

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
// 定義選擇事件的函數
function addSelectableListener(li) {
    li.addEventListener("click", function() {
        // 取消之前選中的分類
        if (selectedCategory) {
            selectedCategory.classList.remove("selected");
        }
        // 設定新的選中分類
        selectedCategory = li;
        selectedCategory.classList.add("selected"); // 標記為選中
        console.log("選中的分類: ", selectedCategory.textContent); // 紀錄被選中的分類

        // 如果是 "新增分類"，清空輸入框
        if (selectedCategory.textContent === "新增分類") {
            document.getElementById("category-name").value = ""; // 清空輸入框
        }
    });
}

// 定義儲存按鈕的事件處理函數
document.querySelector('.save').addEventListener('click', function() {
    const categoryNameInput = document.getElementById("category-name").value;

    if (selectedCategory) {
        if (selectedCategory.textContent === "新增分類") {
            // 新增分類的情況
            if (categoryNameInput.trim() === "") {
                alert("請輸入分類名稱！");
                return;
            }

            const apiUrl = '/api/1.0/category/create';
            const body = {
                type: parseInt(type), // 將 type 轉換為整數
                name: categoryNameInput,
                lineUserId: lineUserId
            };

            // 發送 POST 請求
            fetch(apiUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(body)
            })
                .then(response => {
                    if (response.ok) {
                        return response.json();
                    }
                    throw new Error('新增失敗');
                })
                .then(data => {
                    console.log("新增成功: ", data);
                    fetchCategories();
                })
                .catch(error => {
                    console.error("發生錯誤: ", error);
                });
        } else {
            // 編輯已選中的分類
            if (categoryNameInput.trim() === "") {
                alert("請輸入分類名稱！");
                return;
            }

            const apiUrl = '/api/1.0/category/update';
            const categoryId = selectedCategory.dataset.id;

            const body = {
                id: categoryId,
                type: parseInt(type),
                name: categoryNameInput,
                lineUserId: lineUserId
            };

            // 發送 PUT 請求
            fetch(apiUrl, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(body)
            })
                .then(response => {
                    if (response.ok) {
                        return response.json();
                    }
                    throw new Error('更新失敗');
                })
                .then(data => {
                    console.log("更新成功: ", data);
                    fetchCategories();
                })
                .catch(error => {
                    console.error("發生錯誤: ", error);
                });
        }
    } else {
        alert("請選擇一個分類進行編輯或新增！");
    }
});