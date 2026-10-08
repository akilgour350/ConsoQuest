const express = require('express');
const app = express();

app.use(express.json());

app.get('/', (req, res) => {
    res.send('hello from Node');
});

app.get('/status', (req, res) => {
    console.log('Received request in /status!');
    res.sendStatus(200);
});

const PORT = 18080;
app.listen(PORT, () => console.log('Server running on port 18080'));