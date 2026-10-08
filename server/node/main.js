import pg from 'pg';

// PostGres initialisation
const { Pool, Client } = pg;
const pool = new Pool({
    user: 'postgres',
    database: 'consodb',
    host: process.env.DB_HOST,
    port: process.env.DB_PORT,
    password: process.env.DB_PASSWORD
});

// used for building and running the API
const express = require('express');
const app = express();
app.use(express.json());

async function runQuery(query) {
    console.log('Running query...');
    console.log('SELECT * FROM players WHERE username = $1', username);
    return await pool.query(query);    
}

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
        const result = runQuery('SELECT * FROM players WHERE username = $1', username).then(() => console.log('Query complete!'));
        console.log(result);
    } catch (error) {
        res.status(500).send(error);
    }
});

const PORT = 18080;
app.listen(PORT, () => console.log('Server running on port 18080'));