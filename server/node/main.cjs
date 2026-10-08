// used for building and running the API
const express = require('express');
const app = express();
app.use(express.json());

// postgres
const { runQuery } = require('./db');

app.get('/status', (req, res) => {
    res.sendStatus(200);
});

app.post('/login', (req, res) => {
    const { username, password } = req.body;

    if (!username || !password) {
        console.log('400: Bad login request');
        res.status(400).send('Bad login request');
    }

    try {
        const result = runQuery('SELECT * FROM players WHERE username = ' + username).then(() => console.log('Query complete!'));
        console.log(result);
    } catch (error) {
        res.status(500).send(error);
    }
});

const PORT = 18080;
app.listen(PORT, () => console.log('Server running on port 18080'));