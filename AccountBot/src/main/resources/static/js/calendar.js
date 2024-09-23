const currentMonthElement = document.getElementById('currentMonth');
const calendarDaysElement = document.getElementById('calendarDays');
const prevMonthBtn = document.getElementById('prevMonthBtn');
const nextMonthBtn = document.getElementById('nextMonthBtn');

let currentDate = new Date();

// 在網頁加載時執行
document.addEventListener('DOMContentLoaded', function() {
    const checkLineUserId = setInterval(() => {
        if (lineUserId) {
            clearInterval(checkLineUserId);
            console.log("lineUserId : " + lineUserId);

            updateCalendar();
        }
    }, 100);

    const urlParams = new URLSearchParams(window.location.search);
    type = urlParams.get('type');
});

// 檢查是否為閏年
function isLeapYear(year) {
    return (year % 4 === 0 && year % 100 !== 0) || (year % 400 === 0);
}

// 取得某月的天數，並考慮閏年
function getDaysInMonth(year, month) {
    if (month === 1) { // 2月，考慮閏年
        return isLeapYear(year) ? 29 : 28;
    } else {
        return [31, 30, 31, 30, 31, 31, 30, 31, 30, 31, 30, 31][month];
    }
}

// 更新日曆顯示
function updateCalendar() {
    const year = currentDate.getFullYear();
    const month = currentDate.getMonth();

    currentMonthElement.textContent = `${year}年${('0' + (month + 1)).slice(-2)}月`;

    // 清空先前的日曆內容
    calendarDaysElement.innerHTML = '';

    // 計算該月的第一天和最後一天
    const firstDay = new Date(year, month, 1);
    const daysInMonth = getDaysInMonth(year, month);

    // 取得當月第一天是星期幾
    const firstDayOfWeek = firstDay.getDay();

    // 先生成空白天數以對齊該月第一天的星期幾
    for (let i = 0; i < firstDayOfWeek; i++) {
        const emptyDay = document.createElement('div');
        calendarDaysElement.appendChild(emptyDay);
    }

    // 在日曆上顯示天數
    for (let day = 1; day <= daysInMonth; day++) {
        const dayElement = document.createElement('div');
        dayElement.classList.add('calendar-day');
        dayElement.textContent = day;

        // 點擊日期後做某些事情（比如載入當天的記帳資料）
        dayElement.addEventListener('click', () => {
            document.querySelectorAll('.calendar-day').forEach(el => el.classList.remove('active'));
            dayElement.classList.add('active');
            loadExpensesForDay(day);
        });

        calendarDaysElement.appendChild(dayElement);
    }
}

// 切換月份
prevMonthBtn.addEventListener('click', () => {
    currentDate.setMonth(currentDate.getMonth() - 1);
    // 如果月份小於 0，減少年份並將月份設為 11（即 12月）
    if (currentDate.getMonth() < 0) {
        currentDate.setFullYear(currentDate.getFullYear() - 1);
        currentDate.setMonth(11); // 12月
    }
    updateCalendar();
});

nextMonthBtn.addEventListener('click', () => {
    currentDate.setMonth(currentDate.getMonth() + 1);
    // 如果月份大於 11，增加年份並將月份設為 0（即 1月）
    if (currentDate.getMonth() > 11) {
        currentDate.setFullYear(currentDate.getFullYear() + 1);
        currentDate.setMonth(0); // 1月
    }
    updateCalendar();
});

function fetchTransactions(startDate, endDate, lineUserId) {
    return fetch(`/api/1.0/transaction/get/details?startDate=${startDate}&endDate=${endDate}&lineUserId=${lineUserId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('網路錯誤：無法獲取記帳資料');
            }
            return response.json(); // 返回 JSON 格式的資料
        });
}

function updateCalendar() {
    const year = currentDate.getFullYear();
    const month = currentDate.getMonth();

    currentMonthElement.textContent = `${year}年${('0' + (month + 1)).slice(-2)}月`;

    // 清空先前的日曆內容
    calendarDaysElement.innerHTML = '';

    // 計算該月的第一天和最後一天
    const firstDay = new Date(year, month, 1);
    const daysInMonth = getDaysInMonth(year, month);
    const startDate = `${year}-${('0' + (month + 1)).slice(-2)}-01`;
    const endDate = `${year}-${('0' + (month + 1)).slice(-2)}-${daysInMonth}`;

    // 獲取記帳資料
    fetchTransactions(startDate, endDate, lineUserId)
        .then(transactions => {
            const daysWithTransactions = new Set(transactions.map(tx => new Date(tx.date).getDate())); // 假設交易資料中有日期屬性

            // 取得當月第一天是星期幾
            const firstDayOfWeek = firstDay.getDay();

            // 先生成空白天數以對齊該月第一天的星期幾
            for (let i = 0; i < firstDayOfWeek; i++) {
                const emptyDay = document.createElement('div');
                calendarDaysElement.appendChild(emptyDay);
            }

            // 在日曆上顯示天數
            for (let day = 1; day <= daysInMonth; day++) {
                const dayElement = document.createElement('div');
                dayElement.classList.add('calendar-day');
                dayElement.textContent = day;

                // 如果這一天有記帳，則添加特殊樣式
                if (daysWithTransactions.has(day)) {
                    dayElement.classList.add('has-transaction'); // 可以自定義樣式
                }

                // 點擊日期後做某些事情（比如載入當天的記帳資料）
                dayElement.addEventListener('click', () => {
                    document.querySelectorAll('.calendar-day').forEach(el => el.classList.remove('active'));
                    dayElement.classList.add('active');
                    loadExpensesForDay(day);
                });

                calendarDaysElement.appendChild(dayElement);
            }
        })
        .catch(error => {
            console.error(error);
            alert('載入記帳資料失敗，請稍後再試。');
        });
}


function loadExpensesForDay(day) {
    const year = currentDate.getFullYear();
    const month = currentDate.getMonth() + 1;
    const date = ('0' + day).slice(-2);
    const targetDate = `${year}-${('0' + month).slice(-2)}-${date}`;

    fetch(`/api/1.0/transaction/get/details?startDate=${targetDate}&endDate=${targetDate}&lineUserId=${lineUserId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('網路錯誤：無法獲取記帳資料');
            }
            return response.json();
        })
        .then(data => {
            const expenseDetailsElement = document.getElementById('expenseDetails');
            expenseDetailsElement.innerHTML = '';

            if (data.length === 0) {
                expenseDetailsElement.textContent = `在${targetDate}沒有記帳資料。`;
            } else {
                const detailsList = document.createElement('ul');
                data.forEach(expense => {
                    const listItem = document.createElement('li');
                    const typeText = expense.type === 0 ? '收入' : '支出';
                    listItem.textContent = `${expense.date} ${expense.description} ${expense.cost} ${typeText}`;

                    const viewDetailsBtn = document.createElement('button');
                    viewDetailsBtn.textContent = '查看詳細';
                    viewDetailsBtn.onclick = () => {
                        window.location.href = `transactionDetail.html?lineUserId=${lineUserId}&transactionId=${expense.id}`;
                    };

                    listItem.appendChild(viewDetailsBtn);
                    detailsList.appendChild(listItem);
                });
                expenseDetailsElement.appendChild(detailsList);


                const goRecordElement = document.getElementById('goRecord');
                goRecordElement.innerHTML = '';
                const goToRecordBtn = document.createElement('button');
                goToRecordBtn.textContent = '去記帳';
                goToRecordBtn.onclick = () => {
                    window.location.href = `/record.html?date=${targetDate}`;
                };
                goRecordElement.appendChild(goToRecordBtn);

            }
        })
        .catch(error => {
            console.error(error);
            const expenseDetailsElement = document.getElementById('expenseDetails');
            expenseDetailsElement.textContent = '載入記帳資料失敗，請稍後再試。';
        });
}
