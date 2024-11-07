const socket = new WebSocket("ws://localhost:8080/your-app-name/game");
let player = "X";
let room = prompt("Enter room name:");
socket.onopen = () => { socket.send(`join,${room}`); };
socket.onmessage = (event) => {
    const [action, ...args] = event.data.split(",");
    switch (action) {
        case "joined": document.getElementById("message").textContent = "Game started. You are " + player; break;
        case "move": const [index, player] = args;
            document.querySelectorAll(".cell")[index].textContent = player; break;
        case "win": document.getElementById("message").textContent = `${args[0]} wins!`; break;
        case "draw": document.getElementById("message").textContent = "It's a draw!"; break;
        case "end": document.getElementById("message").textContent = args[0]; break;
    }
};
document.querySelectorAll(".cell").forEach((cell, index) => {
    cell.addEventListener("click", () => { socket.send(`move,${room},${index},${player}`); });
});