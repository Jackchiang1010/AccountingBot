let lineUserId = "";
let isLoginInProgress = false;

// 在網頁加載時執行
window.onload = function() {

    // 檢查 URL 是否包含授權碼
    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get('code');

    if (code) {
        // 如果 URL 包含授權碼，處理回調
        handleCallback(code);
    } else {
        // 否則，檢查是否已經有有效的 access token
        checkAndUseAccessToken();
    }
};

function checkAndUseAccessToken() {
    let accessToken = localStorage.getItem('lineAccessToken');
    let tokenExpireTime = localStorage.getItem('lineTokenExpireTime');
    let refreshToken = localStorage.getItem('lineRefreshToken');

    console.log("accessToken : " + accessToken);
    console.log("tokenExpireTime : " + tokenExpireTime);

    if (accessToken && tokenExpireTime && new Date().getTime() < parseInt(tokenExpireTime)) {
        // 如果有有效的 access token，直接使用它
        getUserProfile(accessToken);
    } else if (refreshToken) {
        // 如果 access token 已過期，使用 refresh token 獲取新的 access token
        refreshAccessToken(refreshToken);
    } else {
        // 如果沒有有效的 access token，開始 LINE Login 流程
        startLineLogin();
    }
}

function refreshAccessToken(refreshToken) {
    fetch('https://api.line.me/oauth2/v2.1/token', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: new URLSearchParams({
            grant_type: 'refresh_token',
            refresh_token: refreshToken,
            client_id: '2006338412',
            client_secret: '6abc1c7960246f49092cc5f2509fe267'
        })
    })
        .then(response => response.json())
        .then(data => {
            // 存儲新的 access token 和過期時間
            localStorage.setItem('lineAccessToken', data.access_token);
            localStorage.setItem('lineTokenExpireTime', new Date().getTime() + data.expires_in * 1000);
            // 重新獲取用戶資料
            getUserProfile(data.access_token);
        })
        .catch(error => {
            console.error('Error:', error);
            // 如果 refresh token 也過期，引導用戶重新登入
            startLineLogin();
        });
}

function startLineLogin() {
    if (isLoginInProgress) {
        console.log('Login already in progress');
        return;
    }

    isLoginInProgress = true;

    // LINE Login 的設定
    const clientId = '2006338412'; // 替換為您的 LINE Login Channel ID
    const redirectUri = "https://jacktest.site/index.html"; // 使用當前頁面 URL
    const state = generateRandomString(); // 生成隨機字符串作為 state
    const scope = 'profile%20openid'; // 請求的權限範圍

    // 儲存 state 用於後續驗證
    localStorage.setItem('lineLoginState', state);

    console.log("state : " + state);

    // 構建授權 URL
    const authUrl = `https://access.line.me/oauth2/v2.1/authorize?response_type=code&client_id=${clientId}&redirect_uri=${redirectUri}&state=${state}&scope=${scope}`;

    // 將使用者重定向到授權 URL
    window.location.href = authUrl;
}

function handleCallback(code) {
    // 不檢查 state
    // const state = localStorage.getItem('lineLoginState');
    // const urlParams = new URLSearchParams(window.location.search);
    // const receivedState = urlParams.get('state');

    // 清除儲存的 state
    localStorage.removeItem('lineLoginState');

    // 使用授權碼獲取 access token
    getAccessToken(code);

    // 清除 URL 中的參數
    window.history.replaceState({}, document.title, window.location.pathname);
}

function getAccessToken(code) {
    fetch('https://api.line.me/oauth2/v2.1/token', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: new URLSearchParams({
            grant_type: 'authorization_code',
            code: code,
            redirect_uri: "https://jacktest.site/index.html",
            client_id: '2006338412',
            client_secret: '6abc1c7960246f49092cc5f2509fe267'
        })
    })
        .then(response => response.json())
        .then(data => {
            // 存儲 access token、過期時間和 refresh token
            localStorage.setItem('lineAccessToken', data.access_token);
            localStorage.setItem('lineTokenExpireTime', new Date().getTime() + data.expires_in * 1000);
            localStorage.setItem('lineRefreshToken', data.refresh_token); // 儲存 refresh token

            // 使用 access token 獲取用戶資料
            getUserProfile(data.access_token);

            // 清除 URL 中的參數
            window.history.replaceState({}, document.title, window.location.pathname);
        })
        .catch(error => console.error('Error:', error))
        .finally(() => {
            isLoginInProgress = false;
        });
}

function getUserProfile(accessToken) {
    fetch('https://api.line.me/v2/profile', {
        headers: {
            'Authorization': `Bearer ${accessToken}`
        }
    })
        .then(response => response.json())
        .then(profile => {
            lineUserId = profile.userId;
        })
        .catch(error => console.error('Error:', error));
}

function generateRandomString() {
    return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
}