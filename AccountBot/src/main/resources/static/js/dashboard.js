let type = 1;
let time = 'month';
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
            drawBalanceChart();
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
    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');

    startDate = startDateInput.value;
    endDate = endDateInput.value;

    // 防呆機制：檢查結束日期是否早於開始日期
    if (new Date(startDate) > new Date(endDate)) {
        alert("結束日期不能早於開始日期");
        endDateInput.value = startDate; // 將結束日期設置為開始日期
        endDate = startDate; // 更新 endDate 變數
    }

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
            backgroundColor: [],
            borderColor: '#4B4B4B'
        }]
    },
    options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                display: true,
                position: 'right', // 將標籤移至圖表右側
                labels: {
                    usePointStyle: true, // 使用點樣式顯示標籤
                    padding: 20, // 調整標籤與圖表的間距
                    font: {
                        size: 16 // 標籤字體大小
                    }
                }
            }
        }
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
            const labels = data.data.map(item => `${item.category} $${item.total_cost}`);
            const dataValues = data.data.map(item => item.total_cost);

            // 根據類型選擇顏色（收入 vs 支出）
            const backgroundColors = type === 1 ?
                [
                    '#ffadad',
                    '#ffc2a9',
                    '#ffd6a5',
                    '#fdffb6',
                    '#caffbf',
                    '#b3fbdf',
                    '#aae0ef',
                    '#a0c4ff',
                    '#bdb2ff',
                    '#ffc6ff'
                ] :
                [
                    '#f29e4c',
                    '#f1c453',
                    '#efea5a',
                    '#b9e769',
                    '#83e377',
                    '#16db93',
                    '#0db39e',
                    '#048ba8',
                    '#2c699a',
                    '#54478c'
                ];

            expenseChart.data.labels = labels;
            expenseChart.data.datasets[0].data = dataValues;
            expenseChart.data.datasets[0].backgroundColor = backgroundColors;
            expenseChart.update();

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

                        allCategories.forEach((category, index) => {
                            // 建立可點擊的類別名稱，並加上事件
                            const categoryContainer = document.createElement('div');
                            categoryContainer.className = 'category-container';
                            categoryContainer.style.position = 'relative';
                            categoryContainer.style.backgroundColor = getCategoryColorFromChart(index); // 根據圖表顏色設置背景顏色
                            categoryContainer.style.padding = '10px';
                            categoryContainer.style.cursor = 'pointer';

                            const categoryTitle = document.createElement('strong');
                            categoryTitle.textContent = `${category} $0`; // 預設金額為0
                            categoryContainer.appendChild(categoryTitle);

                            // 建立用來放帳務明細的 ul 元素
                            const detailsContainer = document.createElement('ul');
                            detailsContainer.style.display = 'none'; // 預設隱藏

                            categoryContainer.addEventListener('click', () => {
                                // 切換明細的顯示狀態
                                detailsContainer.style.display = detailsContainer.style.display === 'none' ? 'block' : 'none';
                            });

                            // 過濾出該類別的帳務明細
                            const filteredData = transactionData.filter(item => item.category === category);
                            let totalCost = 0;
                            if (filteredData.length > 0) {
                                filteredData.forEach(item => {
                                    const detailItem = document.createElement('li');
                                    detailItem.innerHTML = `
                                        <span style="flex: 1;">${item.date} ${item.description} - $${item.cost}</span>
                                        <button class="edit-btn" data-id="${item.id}" onclick="editTransaction(lineUserId, ${item.id})" style="margin-left: auto">修改</button>`;

                                    // 設定每個帳務明細項目的樣式
                                    detailItem.style.display = 'flex'; // 使用 flex 排列
                                    detailItem.style.justifyContent = 'space-between'; // 左右對齊
                                    detailItem.style.alignItems = 'center'; // 垂直置中
                                    detailItem.style.backgroundColor = '#ffffff'; // 白色底色
                                    detailItem.style.padding = '10px'; // 設定內距
                                    detailItem.style.width = '100%'; // 設定寬度為 100%
                                    detailItem.style.boxSizing = 'border-box'; // 確保 padding 不影響元素寬度
                                    detailItem.style.border = '1px solid #ddd'; // 添加邊框以區分

                                    totalCost += item.cost; // 計算該類別的總金額
                                    detailsContainer.appendChild(detailItem);
                                });
                            } else {
                                const noDataItem = document.createElement('li');
                                noDataItem.textContent = '無記帳';

                                // 設定 "無記帳" 項目的樣式
                                noDataItem.style.backgroundColor = '#ffffff'; // 白色底色
                                noDataItem.style.padding = '10px'; // 設定內距
                                noDataItem.style.width = '100%'; // 設定寬度為 100%
                                noDataItem.style.boxSizing = 'border-box'; // 確保 padding 不影響元素寬度
                                noDataItem.style.border = '1px solid #ddd'; // 添加邊框以區分

                                detailsContainer.appendChild(noDataItem);
                            }

                            // 更新該類別的總金額顯示
                            categoryTitle.textContent = `${category} $${totalCost}`;

                            // 將類別名稱和明細容器添加到列表
                            detailList.appendChild(categoryContainer);
                            detailList.appendChild(detailsContainer);
                        });
                    } else {
                        console.error('Invalid transaction data');
                    }
                });
        })
        .catch(error => console.error('Error fetching category or transaction details:', error));
}

// 從圖表中抓取顏色
function getCategoryColorFromChart(index) {
    if (expenseChart && expenseChart.data && expenseChart.data.datasets[0]) {
        return expenseChart.data.datasets[0].backgroundColor[index] || '#ffffff'; // 若圖表中無顏色，預設為白色
    }
    return '#ffffff';
}

function editTransaction(lineUserId, transactionId) {
    console.log(`Editing transaction`);
    window.location.href = `transactionDetail.html?lineUserId=${lineUserId}&transactionId=${transactionId}`;
}

// 當時間篩選按鈕被點擊時
document.querySelectorAll('.filter-buttons .btn').forEach(button => {
    if (button.id !== 'exportBtn') { // 排除匯出按鈕
        button.addEventListener('click', function() {
            time = this.value;
            setDateRange(); // 更新日期範圍
            updateChart(); // 更新圖表
        });
    }
});

// 當支出/收入按鈕被點擊時
document.querySelectorAll('.category-buttons .btn').forEach(button => {
    button.addEventListener('click', function() {
        type = parseInt(this.value);
        updateChart(); // 更新圖表
    });
});

// 匯出按鈕點擊事件
document.getElementById('exportBtn').addEventListener('click', function() {
    const exportApiUrl = `/api/1.0/S3/export?startDate=${startDate}&endDate=${endDate}&lineUserId=${lineUserId}`;

    fetch(exportApiUrl)
        .then(response => {
            return response.text(); // 取得回傳的文字（URL）
        })
        .then(url => {
            if (url.startsWith('http')) {
                // 透過新增的連結下載 CSV
                const downloadLink = document.createElement('a');
                downloadLink.href = url;
                downloadLink.download = '記帳資料.csv'; // 可選，設定下載的檔案名稱
                downloadLink.click();
            } else {
                alert('匯出失敗，請稍後再試');
            }
        })
        .catch(error => {
            console.error('Error exporting data:', error);
            alert('匯出過程中出現錯誤，請稍後再試');
        });
});

// 本月結餘圖表
function drawBalanceChart() {
    fetch(`/api/1.0/transaction/balance?lineUserId=${lineUserId}`)
        .then(response => response.json())
        .then(data => {
            const totalIncome = data.totalIncome;
            const totalExpenses = data.totalExpenses;
            const balance = totalIncome - totalExpenses;

            const ctx = document.getElementById('balanceChart').getContext('2d');
            window.balanceChart = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: ['支出', '收入', '結餘'],
                    datasets: [{
                        data: [totalExpenses, totalIncome, balance],
                        backgroundColor: ['#f08080', '#90ee90', '#fdd835'],
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: true,
                    plugins: {
                        legend: { display: false }
                    },
                    scales: {
                        x: {
                            title: {
                                display: true,
                                text: '本月結餘'
                            }
                        },
                        y: {
                            beginAtZero: true
                        }
                    }
                }
            });
        })
        .catch(error => console.error('Error fetching data:', error));
}
