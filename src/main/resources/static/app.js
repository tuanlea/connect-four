const ROWS = 6;
const COLS = 7;
let stompClient = null;

// Manage Player ID
let playerId = sessionStorage.getItem('playerId');
if (!playerId) {
    playerId = Math.random().toString(36).substring(2, 15);
    sessionStorage.setItem('playerId', playerId);
}
let myRole = 0; // 0=Spectator, 1=RED, 2=YELLOW

function initBoard() {
    const board = document.getElementById('game-board');
    board.innerHTML = '';
    
    for (let c = 0; c < COLS; c++) {
        const colDiv = document.createElement('div');
        colDiv.classList.add('board-column');
        colDiv.dataset.col = c;
        colDiv.addEventListener('click', () => makeMove(c));
        
        for (let r = 0; r < ROWS; r++) {
            const cellDiv = document.createElement('div');
            cellDiv.classList.add('cell');
            cellDiv.dataset.row = r;
            colDiv.appendChild(cellDiv);
        }
        board.appendChild(colDiv);
    }
}

function connect() {
    const socket = new SockJS('/game-websocket');
    stompClient = Stomp.over(socket);
    
    stompClient.debug = null;
    
    stompClient.connect({}, function (frame) {
        document.getElementById('status').innerText = 'Connected! Waiting for game...';
        document.getElementById('status').style.color = 'var(--neon-blue)';
        
        stompClient.subscribe('/topic/game.state', function (message) {
            handleGameState(JSON.parse(message.body));
        });
        
        // Fetch initial state and join
        stompClient.send("/app/game.join", {}, JSON.stringify({ 'playerId': playerId, 'column': 0 }));
    }, function(error) {
        document.getElementById('status').innerText = 'Disconnected. Reconnecting...';
        document.getElementById('status').style.color = 'var(--neon-red)';
        setTimeout(connect, 5000);
    });
}

function makeMove(col) {
    if (stompClient && stompClient.connected) {
        stompClient.send("/app/game.move", {}, JSON.stringify({ 'column': col, 'playerId': playerId }));
    }
}

function handleGameState(state) {
    if (state.board) {
        renderBoard(state.board);
    }
    
    const statusDiv = document.getElementById('status');
    
    // Determine role
    if (state.player1Id === playerId) myRole = 1;
    else if (state.player2Id === playerId) myRole = 2;
    else myRole = 0;

    let roleText = "";
    if (myRole === 1) roleText = " (You are RED)";
    else if (myRole === 2) roleText = " (You are YELLOW)";
    else roleText = " (Spectator)";

    if (state.message) {
        statusDiv.innerText = state.message + roleText;
        statusDiv.style.color = 'var(--neon-red)';
    } else {
        if (state.status === 'IN_PROGRESS') {
            const color = state.currentPlayer === 1 ? 'Red' : 'Yellow';
            statusDiv.innerText = `Current Turn: Player ${state.currentPlayer} (${color})${roleText}`;
            statusDiv.style.color = state.currentPlayer === 1 ? 'var(--neon-red)' : 'var(--neon-yellow)';
        } else if (state.status === 'PLAYER_1_WON') {
            statusDiv.innerText = `Winner: Player 1 (Red)!${roleText}`;
            statusDiv.style.color = 'var(--neon-red)';
        } else if (state.status === 'PLAYER_2_WON') {
            statusDiv.innerText = `Winner: Player 2 (Yellow)!${roleText}`;
            statusDiv.style.color = 'var(--neon-yellow)';
        } else if (state.status === 'DRAW') {
            statusDiv.innerText = `It's a Draw!${roleText}`;
            statusDiv.style.color = 'var(--neon-blue)';
        }
    }
}

function renderBoard(serverBoard) {
    const boardCols = document.querySelectorAll('.board-column');
    
    for (let c = 0; c < COLS; c++) {
        const colDiv = boardCols[c];
        const existingTokens = Array.from(colDiv.querySelectorAll('.token'));
        
        let tokensInCol = [];
        for (let r = ROWS - 1; r >= 0; r--) {
            if (serverBoard[r] && serverBoard[r][c] !== 0) {
                tokensInCol.push(serverBoard[r][c]);
            }
        }

        // If the server has fewer tokens, it means the game was reset. Clear the DOM tokens.
        if (tokensInCol.length < existingTokens.length) {
            existingTokens.forEach(t => t.remove());
            existingTokens.length = 0;
        }

        for (let i = existingTokens.length; i < tokensInCol.length; i++) {
            const playerNum = tokensInCol[i];
            const playerClass = playerNum === 1 ? 'RED' : 'YELLOW';
            const token = document.createElement('div');
            token.classList.add('token', playerClass);
            token.style.bottom = `${10 + i * 75}px`;
            colDiv.appendChild(token);
        }
    }
}

document.getElementById('join-btn').addEventListener('click', () => {
    if (stompClient && stompClient.connected) {
        stompClient.send("/app/game.join", {}, JSON.stringify({}));
    }
});

document.getElementById('reset-btn').addEventListener('click', () => {
    if (stompClient && stompClient.connected) {
        stompClient.send("/app/game.reset", {}, JSON.stringify({}));
    }
});

initBoard();
connect();
