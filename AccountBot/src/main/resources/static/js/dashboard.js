// let lineUserId = ""; // 使用者 ID
let type = 1; // 預設為「支出」
let time = 'month'; // 預設為「今天」
let category = 'all';
let endDate = new Date();
let startDate = '';

// 在網頁加載時執行
document.addEventListener('DOMContentLoaded', function() {
    const checkLineUserId = setInterval(() => {
        if (lineUserId) {
            clearInterval(checkLineUserId); // 清除檢查
            setDateRange();
            updateChart();
            console.log("lineUserId : " + lineUserId);
        }
    }, 100); // 每100毫秒檢查一次
});


// 獲取今天的日期
function getTodayDate() {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0'); // 月份從0開始
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

// 設定日期輸入框的值
function setDateRange() {
    const today = getTodayDate();

    switch (time) {
        case 'today':
            startDate = today;
            endDate = today;
            break;
        case 'week':
            startDate = new Date();
            startDate.setDate(startDate.getDate() - startDate.getDay()); // 本週的第一天
            startDate = startDate.toISOString().split('T')[0];
            endDate = getTodayDate();
            break;
        case 'month':
            startDate = new Date();
            startDate.setDate(1); // 本月的第一天
            startDate = startDate.toISOString().split('T')[0];
            endDate = getTodayDate();
            break;
        case 'lastMonth':
            startDate = new Date();
            startDate.setMonth(startDate.getMonth() - 1);
            startDate.setDate(1); // 上月的第一天
            startDate = startDate.toISOString().split('T')[0];
            endDate = new Date();
            endDate.setDate(0); // 上月的最後一天
            endDate = endDate.toISOString().split('T')[0];
            break;
        case 'halfYear':
            startDate = new Date();
            startDate.setMonth(startDate.getMonth() - 6);
            startDate.setDate(1); // 半年的第一天
            startDate = startDate.toISOString().split('T')[0];
            endDate = getTodayDate();
            break;
    }

    document.getElementById('startDate').value = startDate;
    document.getElementById('endDate').value = endDate;
}

// 當日期變更時，更新 time 變數
function updateTime() {
    startDate = document.getElementById('startDate').value;
    endDate = document.getElementById('endDate').value;
    time = `custom:${startDate},${endDate}`;
    console.log("Updated time:", time);
    updateChart();
}

// 監聽日期變化事件
document.getElementById('startDate').addEventListener('change', updateTime);
document.getElementById('endDate').addEventListener('change', updateTime);

// 初始化圖表
var ctx = document.getElementById('expenseChart').getContext('2d');
var expenseChart = new Chart(ctx, {
    type: 'pie',
    data: {
        labels: [],
        datasets: [{
            data: [],
            backgroundColor: []
        }]
    },
    options: {
        responsive: true,
        maintainAspectRatio: false
    }
});

// 更新圖表和類別列表的函數
function updateChart() {

    const apiUrl = `/api/1.0/transaction/get?type=${type}&category=${category}&time=${time}&lineUserId=${lineUserId}`;

    console.log("apiUrl : " + apiUrl);

    fetch(apiUrl)
        .then(response => response.json())
        .then(data => {
            // 更新圖表的標籤和數據
            const labels = data.data.map(item => item.category);
            const dataValues = data.data.map(item => item.total_cost);

            // 根據類型選擇顏色（收入 vs 支出）
            const backgroundColors = type === 1 ?
                ['#FF6384', '#36A2EB', '#FFCE56', '#FF9F40', '#4BC0C0'] :  // 支出顏色
                ['#4BC0C0', '#9966FF', '#FF9F40', '#FFCE56', '#36A2EB'];   // 收入顏色

            expenseChart.data.labels = labels;
            expenseChart.data.datasets[0].data = dataValues;
            expenseChart.data.datasets[0].backgroundColor = backgroundColors;
            expenseChart.update();

            // 更新類別列表
            const categoryList = document.querySelector('.category-list');
            categoryList.innerHTML = labels.map((label, index) =>
                `<li><span style="color: ${backgroundColors[index]}">●</span> ${label} $${dataValues[index]}</li>`
            ).join('');

            // 獲取明細資料
            getCategoryDetails();

        })
        .catch(error => console.error('Error fetching data:', error));
}

function getCategoryDetails() {
    const categoryApiUrl = `/api/1.0/category/get?type=${type}&name=all&lineUserId=${lineUserId}`;

    fetch(categoryApiUrl)
        .then(response => response.json())
        .then(categoryData => {
            let allCategories = categoryData.data.map(item => item.name);

            const detailApiUrl = `/api/1.0/transaction/get/details?startDate=${startDate}&endDate=${endDate}&lineUserId=${lineUserId}`;

            return fetch(detailApiUrl)
                .then(response => response.json())
                .then(transactionData => {
                    if (Array.isArray(transactionData)) {
                        // 清空現有的明細列表
                        const detailList = document.querySelector('.card .category-list');
                        detailList.innerHTML = '';

                        allCategories.forEach(category => {
                            const categoryTitle = `<li><strong>${category}</strong></li>`;
                            detailList.innerHTML += categoryTitle;

                            const filteredData = transactionData.filter(item => item.category === category);

                            if (filteredData.length > 0) {
                                filteredData.forEach(item => {
                                    detailList.innerHTML += `
                                        <li>
                                            ${item.date} ${item.description} - $${item.cost}
                                            <button className="edit-btn" data-id="${item.id}" onclick="editTransaction(lineUserId, ${item.id})">修改</button>
                                        </li>`;
                                });
                            } else {
                                detailList.innerHTML += `<li>無記帳</li>`;
                            }
                        });

                    } else {
                        console.error('Invalid transaction data');
                    }
                });
        })
        .catch(error => console.error('Error fetching category or transaction details:', error));
}

function editTransaction(lineUserId, transactionId) {
    console.log(`Editing transaction`);
    window.location.href = `transactionDetail.html?lineUserId=${lineUserId}&transactionId=${transactionId}`;
}

// 當時間篩選按鈕被點擊時
document.querySelectorAll('.filter-buttons .btn').forEach(button => {
    button.addEventListener('click', function() {
        time = this.value;
        setDateRange(); // 更新日期範圍
        updateChart(); // 更新圖表
    });
});

// 當支出/收入按鈕被點擊時
document.querySelectorAll('.category-buttons .btn').forEach(button => {
    button.addEventListener('click', function() {
        type = parseInt(this.value);
        updateChart(); // 更新圖表
    });
});

