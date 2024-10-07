let type = 1;
let time = 'month';
let category = 'all';
let endDate = new Date();
let startDate = '';

const incomeColors = ['#FF6666', '#FFA366', '#FFFF66', '#A3FF66', '#66FF99', '#66FFFF', '#66A3FF', '#A366FF', '#FF66FF', '#FF66A3'];
const expenseColors = ['#66FFCC', '#66FF33', '#CCFF66', '#FFFF33', '#FFCC66', '#FF9966', '#FF66CC', '#9966FF', '#6699FF', '#66CCFF'];

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
            borderColor: '#000000'
        }]
    },
    options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                display: true,
                position: 'right',
                labels: {
                    usePointStyle: true,
                    padding: 10,
                    color: '#000000',
                    font: {
                        size: 18
                    }
                }
            }
        }
    }
});

// 初始化 balanceChart 長條圖
var ctxBalance = document.getElementById('balanceChart').getContext('2d');
var balanceChart = new Chart(ctxBalance, {
    type: 'bar',
    data: {
        labels: ['支出', '收入', '結餘'],
        datasets: [{
            data: [0, 0, 0],
            backgroundColor: ['#f08080', '#90ee90', '#fdd835'],
        }]
    },
    options: {
        responsive: true,
        maintainAspectRatio: true,
        plugins: {
            legend: {
                display: false,
                labels: {
                    color: '#000000'
                }
            },
            tooltip: {
                bodyFont: { size: 24 }
            },
            title: {
                display: true,
                text: '本月結餘',
                font: {
                    size: 24
                },
                color: '#000000',
                padding: {
                    top: 10,
                    bottom: 30
                }
            }
        },
        scales: {
            x: {
                ticks: {
                    font: {
                        size: 24
                    },
                    color: '#000000'
                }
            },
            y: {
                beginAtZero: true,
                ticks: {
                    font: {
                        size: 24
                    },
                    color: '#000000'
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
            let labels = data.data.map(item => `${item.category} $${item.total_cost}`);
            let dataValues = data.data.map(item => item.total_cost);

            // 如果資料為空，設定一個預設的標籤和數值
            if (dataValues.length === 0) {
                labels = ['無資料'];
                dataValues = [1]; // 給一個最小值來繪製圖表
                expenseChart.data.datasets[0].backgroundColor = ['#FFF2CC']; // 設定無資料的顏色
            } else {
                // 根據類型選擇顏色（收入 vs 支出）
                expenseChart.data.datasets[0].backgroundColor = type === 1 ? incomeColors : expenseColors;
            }

            expenseChart.data.labels = labels;
            expenseChart.data.datasets[0].data = dataValues;
            expenseChart.update();

            // 獲取明細資料
            getCategoryDetails();

        })
        .catch(error => console.error('Error fetching data:', error));

    drawBalanceChart();
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
                        const detailList = document.querySelector('.card .category-list');
                        detailList.innerHTML = '';

                        allCategories.forEach((category, index) => {
                            const categoryContainer = document.createElement('div');
                            categoryContainer.className = 'category-container';

                            const colorDot = document.createElement('div');
                            colorDot.className = 'color-dot';
                            colorDot.style.backgroundColor = getCategoryColorFromChart(index); // 使用圖表顏色

                            const categoryTitle = document.createElement('strong');
                            categoryTitle.textContent = `${category} $0`; // 預設金額為0

                            categoryContainer.appendChild(colorDot);
                            categoryContainer.appendChild(categoryTitle);

                            const detailsContainer = document.createElement('ul');
                            detailsContainer.className = 'details-container';

                            categoryContainer.addEventListener('click', () => {
                                detailsContainer.classList.toggle('show');
                            });

                            const filteredData = transactionData.filter(item => item.category === category);
                            let totalCost = 0;
                            if (filteredData.length > 0) {
                                filteredData.forEach(item => {
                                    const detailItem = document.createElement('li');
                                    detailItem.className = 'detail-item';
                                    detailItem.innerHTML = `
                                        <span class="detail-description">${item.date} ${item.description} - $${item.cost}</span>
                                        <button class="edit-btn" data-id="${item.id}" onclick="editTransaction(lineUserId, ${item.id})">修改</button>`;

                                    totalCost += item.cost;
                                    detailsContainer.appendChild(detailItem);
                                });
                            } else {
                                const noDataItem = document.createElement('li');
                                noDataItem.className = 'no-data-item';
                                noDataItem.textContent = '無記帳';
                                detailsContainer.appendChild(noDataItem);
                            }

                            categoryTitle.textContent = `${category} $${totalCost}`;

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


// 從圖表中抓取顏色並自動調整色調、亮度和飽和度
function getCategoryColorFromChart(index) {
    // 使用全域變數的顏色陣列
    const colors = type === 1 ? incomeColors : expenseColors;

    if (colors && colors.length > 0) {
        if (index < 10) {
            // 前10個顏色照原本設定
            return colors[index % colors.length];
        } else {
            // 超過10個後，進行調整
            const colorIndex = index % colors.length;
            let color = colors[colorIndex];
            const adjustmentFactor = Math.floor((index - 10) / 10) + 1; // 每超過 10 個分類進行一次調整
            return adjustColor(color, adjustmentFactor);
        }
    }

    return '#ffffff';
}

// 調整顏色的色調、亮度和飽和度
function adjustColor(hexColor, factor) {
    // 將十六進制顏色轉換為 HSL
    let { h, s, l } = hexToHsl(hexColor);

    // 調整色調、亮度和飽和度
    h = (h + factor * 20) % 360;     // 每次調整 20 度的色相
    s = Math.max(30, s - factor * 5); // 每次降低 5% 的飽和度，最低為 30%
    l = Math.min(90, l + factor * 5); // 每次提高 5% 的亮度，最高為 90%

    // 將 HSL 轉換回十六進制
    return hslToHex(h, s, l);
}

// 將十六進制顏色轉換為 HSL
function hexToHsl(hex) {
    let r = parseInt(hex.substring(1, 3), 16) / 255;
    let g = parseInt(hex.substring(3, 5), 16) / 255;
    let b = parseInt(hex.substring(5, 7), 16) / 255;

    let max = Math.max(r, g, b);
    let min = Math.min(r, g, b);
    let h, s, l = (max + min) / 2;

    if (max === min) {
        h = s = 0; // 灰色
    } else {
        let d = max - min;
        s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
        switch (max) {
            case r: h = (g - b) / d + (g < b ? 6 : 0); break;
            case g: h = (b - r) / d + 2; break;
            case b: h = (r - g) / d + 4; break;
        }
        h *= 60;
    }

    return { h, s: s * 100, l: l * 100 };
}

// 將 HSL 轉換回十六進制顏色
function hslToHex(h, s, l) {
    s /= 100;
    l /= 100;

    const c = (1 - Math.abs(2 * l - 1)) * s;
    const x = c * (1 - Math.abs((h / 60) % 2 - 1));
    const m = l - c / 2;
    let r = 0, g = 0, b = 0;

    if (0 <= h && h < 60) { r = c; g = x; b = 0; }
    else if (60 <= h && h < 120) { r = x; g = c; b = 0; }
    else if (120 <= h && h < 180) { r = 0; g = c; b = x; }
    else if (180 <= h && h < 240) { r = 0; g = x; b = c; }
    else if (240 <= h && h < 300) { r = x; g = 0; b = c; }
    else if (300 <= h && h < 360) { r = c; g = 0; b = x; }

    r = Math.round((r + m) * 255);
    g = Math.round((g + m) * 255);
    b = Math.round((b + m) * 255);

    return `#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}`;
}

function editTransaction(lineUserId, transactionId) {
    console.log(`Editing transaction`);
    window.location.href = `transactionDetail.html?lineUserId=${lineUserId}&transactionId=${transactionId}&from=dashboard`;
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

            balanceChart.data.datasets[0].data = [totalExpenses, totalIncome, balance];
            balanceChart.update();
        })
        .catch(error => console.error('Error fetching data:', error));
}
