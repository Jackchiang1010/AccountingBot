let type;
let selectedCategory;

// 在網頁加載時執行
document.addEventListener('DOMContentLoaded', function() {
    const checkLineUserId = setInterval(() => {
        if (lineUserId) {
            clearInterval(checkLineUserId);
            console.log("lineUserId : " + lineUserId);

            fetchCategories();
        }
    }, 100);

    const urlParams = new URLSearchParams(window.location.search);
    type = urlParams.get('type');
});

function fetchCategories() {
    const categoryList = document.getElementById("category-list");
    const budgetSection = document.querySelector('.budgets');

    if (type == 0) {
        budgetSection.style.display = 'none';
        categoryList.style.textAlign = 'center';
    } else {
        budgetSection.style.display = 'block';
        categoryList.style.textAlign = 'left';
    }

    const apiUrl = `/api/1.0/category/get?type=${type}&name=all&lineUserId=${lineUserId}`;

    categoryList.innerHTML = "";

    fetch(apiUrl)
        .then(response => response.json())
        .then(data => {
            if (data && data.data) {
                data.data.forEach(category => {
                    const li = document.createElement("li");
                    li.textContent = category.name;
                    li.dataset.id = category.id;
                    li.classList.add("selectable");
                    addSelectableListener(li);
                    categoryList.appendChild(li);
                });

                const addCategoryLi = document.createElement("li");
                addCategoryLi.textContent = "新增分類";
                addCategoryLi.classList.add("selectable");
                addSelectableListener(addCategoryLi);
                categoryList.appendChild(addCategoryLi);

                fetchBudgets(data.data.map(c => c.name));
            } else {
                console.error("無法取得分類資料");
            }
        })
        .catch(error => {
            console.error("發生錯誤: ", error);
        });
}

function fetchBudgets(categories) {
    const budgetList = document.getElementById("budget-list");

    const apiUrl = `/api/1.0/budget/get?category=all&lineUserId=${lineUserId}`;

    fetch(apiUrl)
        .then(response => response.json())
        .then(data => {
            const budgets = {};

            if (data && data.data) {
                data.data.forEach(budget => {
                    budgets[budget.category] = budget;
                });
            } else {
                console.error("無法取得預算資料");
            }

            budgetList.innerHTML = '';

            categories.forEach(category => {
                const budget = budgets[category]; // 可能為 undefined
                const li = document.createElement("li");

                const input = document.createElement("input");
                input.type = "number";
                input.value = budget ? budget.price : ''; // 如果有預算則設置預設值，否則不設值
                input.placeholder = "輸入預算";
                input.dataset.category = category; // 記錄類別名稱

                const updateButton = document.createElement("button");
                updateButton.textContent = "更新";
                updateButton.addEventListener("click", () => {
                    const inputValue = input.value.trim();

                    if (inputValue === "") {
                        if (budget) {
                            const body = {
                                id: budget.id
                            };

                            fetch('/api/1.0/budget/delete', {
                                method: 'DELETE',
                                headers: {
                                    'Content-Type': 'application/json'
                                },
                                body: JSON.stringify(body)
                            })
                                .then(response => {
                                    if (response.ok) {
                                        return response.json();
                                    }
                                    throw new Error('刪除預算失敗');
                                })
                                .then(data => {
                                    console.log("預算刪除成功: ", data);
                                    fetchCategories();
                                })
                                .catch(error => {
                                    console.error("發生錯誤: ", error);
                                });
                        } else {
                            console.log("無預算可刪除");
                        }
                    } else {
                        if (budget) {
                            const body = {
                                id: budget.id,
                                category: category,
                                price: parseFloat(inputValue),
                                lineUserId: lineUserId
                            };

                            fetch('/api/1.0/budget/update', {
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
                                    throw new Error('更新預算失敗');
                                })
                                .then(data => {
                                    console.log("預算更新成功: ", data);
                                    fetchCategories();
                                })
                                .catch(error => {
                                    console.error("發生錯誤: ", error);
                                });
                        } else {
                            const body = {
                                category: category,
                                price: parseFloat(inputValue),
                                lineUserId: lineUserId
                            };

                            fetch('/api/1.0/budget/create', {
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
                                    throw new Error('創建預算失敗');
                                })
                                .then(data => {
                                    console.log("預算創建成功: ", data);
                                    fetchCategories();
                                })
                                .catch(error => {
                                    console.error("發生錯誤: ", error);
                                });
                        }
                    }
                });

                li.appendChild(input);
                li.appendChild(updateButton);
                budgetList.appendChild(li);
            });
        })
        .catch(error => {
            console.error("發生錯誤: ", error);
        });
}

function addSelectableListener(li) {
    li.addEventListener("click", function() {

        if (selectedCategory) {
            selectedCategory.classList.remove("selected");
        }

        selectedCategory = li;
        selectedCategory.classList.add("selected");
        console.log("選中的分類: ", selectedCategory.textContent);
        document.getElementById("category-name").value = selectedCategory.textContent;
    });
}

document.querySelector('.save').addEventListener('click', function() {
    const categoryNameInput = document.getElementById("category-name").value;

    if (selectedCategory) {
        if (selectedCategory.textContent === "新增分類") {

            if (categoryNameInput.trim() === "") {
                alert("請輸入分類名稱！");
                return;
            }

            const apiUrl = '/api/1.0/category/create';
            const body = {
                type: type,
                name: categoryNameInput,
                lineUserId: lineUserId
            };

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

document.querySelector('.delete').addEventListener('click', function() {
    if (selectedCategory) {
        const categoryId = selectedCategory.dataset.id;

        const apiUrl = `/api/1.0/budget/get?category=${selectedCategory.textContent}&lineUserId=${lineUserId}`;

        fetch(apiUrl)
            .then(response => response.json())
            .then(data => {
                if (data && data.data) {
                    const budgetToDelete = data.data[0];
                    if (budgetToDelete) {
                        const body = {
                            id: budgetToDelete.id
                        };

                        return fetch('/api/1.0/budget/delete', {
                            method: 'DELETE',
                            headers: {
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify(body)
                        });
                    }
                }
                return Promise.resolve();
            })
            .then(response => {
                if (response && response.ok) {
                    return response.json();
                }
                return Promise.resolve();
            })
            .then(() => {
                // 刪除分類
                const apiUrl = '/api/1.0/category/delete';
                const body = {
                    id: categoryId
                };

                return fetch(apiUrl, {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(body)
                });
            })
            .then(response => {
                if (response.ok) {
                    return response.json();
                }
                throw new Error('刪除分類失敗');
            })
            .then(data => {
                console.log("刪除成功: ", data);
                selectedCategory.remove();
                selectedCategory = null;
                fetchCategories();
            })
            .catch(error => {
                console.error("發生錯誤: ", error);
            });
    } else {
        alert("請選擇一個分類進行刪除！");
    }
});