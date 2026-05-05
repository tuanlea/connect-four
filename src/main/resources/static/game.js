import { elements } from './ui.js';
import { sendMove } from './network.js';

export const canvas = document.getElementById('gameCanvas');
export const ctx = canvas.getContext('2d');

export let myUsername = "Anonymous";
export function setUsername(name) { myUsername = name; }

export let myPlayer = {
    // Spawns within an 800x600 safely
    x: Math.floor(Math.random() * (800 - 30)) + 15,
    y: Math.floor(Math.random() * (600 - 30)) + 15,
    color: '#3b82f6',
    radius: 15
};

export const otherPlayers = {};

const keys = {};
window.addEventListener('keydown', (e) => {
    if (document.activeElement !== elements.messageInput && document.activeElement !== elements.usernameInput) {
        keys[e.key] = true;
    }
});
window.addEventListener('keyup', (e) => keys[e.key] = false);

export function startGameLoop() {
    function drawGame() {
        const speed = 3;
        let oldX = myPlayer.x;
        let oldY = myPlayer.y;

        if (keys['ArrowUp'] || keys['w'] || keys['W']) myPlayer.y -= speed;
        if (keys['ArrowDown'] || keys['s'] || keys['S']) myPlayer.y += speed;
        if (keys['ArrowLeft'] || keys['a'] || keys['A']) myPlayer.x -= speed;
        if (keys['ArrowRight'] || keys['d'] || keys['D']) myPlayer.x += speed;

        myPlayer.x = Math.max(myPlayer.radius, Math.min(canvas.width - myPlayer.radius, myPlayer.x));
        myPlayer.y = Math.max(myPlayer.radius, Math.min(canvas.height - myPlayer.radius, myPlayer.y));

        if (myPlayer.x !== oldX || myPlayer.y !== oldY) {
            sendMove(myPlayer.x, myPlayer.y);
        }

        ctx.clearRect(0, 0, canvas.width, canvas.height);

        for (const username in otherPlayers) {
            const p = otherPlayers[username];
            ctx.beginPath();
            ctx.arc(p.x, p.y, 15, 0, Math.PI * 2);
            ctx.fillStyle = '#ef4444';
            ctx.fill();
            ctx.closePath();

            ctx.fillStyle = '#f8fafc';
            ctx.font = '10px Arial';
            ctx.textAlign = 'center';
            ctx.fillText(username, p.x, p.y - 20);
        }

        ctx.beginPath();
        ctx.arc(myPlayer.x, myPlayer.y, myPlayer.radius, 0, Math.PI * 2);
        ctx.fillStyle = myPlayer.color;
        ctx.fill();
        ctx.closePath();

        ctx.fillStyle = '#f8fafc';
        ctx.font = '10px Arial';
        ctx.textAlign = 'center';
        ctx.fillText(myUsername, myPlayer.x, myPlayer.y - 20);

        requestAnimationFrame(drawGame);
    }
    drawGame();
}
