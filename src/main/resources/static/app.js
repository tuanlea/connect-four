// --- HTML ELEMENTS ---
const messagesContainer = document.getElementById('messages');
const messageInput = document.getElementById('messageInput');
const sendButton = document.getElementById('sendButton');

const lobbyOverlay = document.getElementById('lobbyOverlay');
const usernameInput = document.getElementById('usernameInput');
const joinGameBtn = document.getElementById('joinGameBtn');

// --- GAME BOARD VARIABLES ---
const canvas = document.getElementById('gameCanvas');
const ctx = canvas.getContext('2d');

let myPlayer = {
    x: 200,
    y: 125,
    color: '#3b82f6',
    radius: 15
};

// --- GAME VARIABLES ---
let myUsername = "Anonymous";
let socket = null;

// --- GAME LOOP ---
function drawGame() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    ctx.beginPath();
    ctx.arc(myPlayer.x, myPlayer.y, myPlayer.radius, 0, Math.PI * 2);
    ctx.fillStyle = myPlayer.color;
    ctx.fill();
    ctx.closePath();

    requestAnimationFrame(drawGame);
}
// Start rendering right away (it will just sit there while in lobby)
drawGame();

// --- LOBBY LOGIC ---
joinGameBtn.addEventListener('click', () => {
    const enteredName = usernameInput.value.trim();
    if (enteredName) {
        myUsername = enteredName;
        lobbyOverlay.style.display = 'none';
        connectToServer();
    }
});

usernameInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') joinGameBtn.click();
});

// --- CHAT LOGIC ---
function appendMessage(text, type, senderName) {
    const messageEl = document.createElement('div');
    messageEl.classList.add('message');
    
    if (type === 'chat') {
        const isMe = senderName === myUsername;
        messageEl.classList.add(isMe ? 'sent' : 'received');
        messageEl.innerHTML = `<strong>${senderName}:</strong> ${text}`;
    } else {
        messageEl.classList.add('system');
        messageEl.textContent = text;
    }
    messagesContainer.appendChild(messageEl);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

// --- WEBSOCKET CONNECTION LOGIC ---
function connectToServer() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    socket = new WebSocket(`${protocol}//${window.location.host}/echo`);

    socket.addEventListener('open', function (event) {
        messagesContainer.innerHTML = '';
        messageInput.disabled = false;
        sendButton.disabled = false;
        messageInput.focus();
        
        socket.send(JSON.stringify({ type: 'join', username: myUsername }));
    });

    socket.addEventListener('message', function (event) {
        const data = JSON.parse(event.data);
        if (data.type === 'system') appendMessage(data.content, 'system');
        else if (data.type === 'chat') appendMessage(data.content, 'chat', data.username);
    });

    socket.addEventListener('close', function (event) {
        appendMessage('Disconnected from server.', 'system');
        messageInput.disabled = true;
        sendButton.disabled = true;
    });

    socket.addEventListener('error', function (event) {
        appendMessage('Error connecting to server.', 'system');
    });
}

// --- SENDING MESSAGES ---
function sendMessage() {
    const message = messageInput.value.trim();
    if (message && socket) {
        socket.send(JSON.stringify({ type: 'chat', content: message }));
        messageInput.value = '';
    }
}

sendButton.addEventListener('click', sendMessage);
messageInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') sendMessage();
});
