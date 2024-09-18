
// 在網頁加載時執行
window.onload = function() {
    // 檢查是否已經完成過 LIFF 初始化
    if (localStorage.getItem('liffInitialized') === 'true') {
        // 如果已經完成過初始化，不再執行任何操作
        return;
    }

    // 初始化 LIFF
    liff.init({ liffId: '2006338412-qNr4rdkr' })
        .then(() => {
            if (!liff.isLoggedIn()) {
                // 若使用者尚未登入，跳轉到 LINE Login
                liff.login();
            } else {
                // 已登入，取得使用者資料
                liff.getProfile().then(profile => {
                    const userId = profile.userId;
                    // 設置本地儲存，標記已經完成過初始化
                    localStorage.setItem('liffInitialized', 'true');
                    // 跳轉到指定頁面，附加 lineUserId
                    window.location.href = `https://f617-2001-b011-3800-3b0e-14ac-161a-3b1b-1983.ngrok-free.app/dashboard.html?lineUserId=${userId}`;
                });
            }
        })
        .catch(err => {
            console.error('LIFF 初始化失敗:', err);
        });
};


var ctx = document.getElementById('expenseChart').getContext('2d');
var expenseChart = new Chart(ctx, {
    type: 'pie',
    data: {
        labels: [],
        datasets: [{
            data: [],
            backgroundColor: ['#FF6384', '#FF9F40', '#FFCD56', '#4BC0C0', '#36A2EB', '#9966FF']
        }]
    },
    options: {
        responsive: true,
        maintainAspectRatio: false
    }
});

// 從 API 獲取資料
fetch('https://jacktest.site/api/1.0/transaction/get?type=1&category=all&time=week&lineUserId=U6f5de7e36ccc512ef1b72bf881e87e54')
    .then(response => response.json())
    .then(data => {
        // 更新圖表的標籤和數據
        const labels = data.data.map(item => item.category);
        const dataValues = data.data.map(item => item.total_cost);

        expenseChart.data.labels = labels;
        expenseChart.data.datasets[0].data = dataValues;
        expenseChart.update();
    })
    .catch(error => console.error('Error fetching data:', error));