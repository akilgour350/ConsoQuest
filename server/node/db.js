import pg from 'pg';

// PostGres initialisation
const { Pool } = pg;
const pool = new Pool({
    user: 'postgres',
    database: 'consodb',
    host: process.env.DB_HOST,
    port: process.env.DB_PORT,
    password: process.env.DB_PASSWORD
});

export async function runQuery(query) {
    console.log('Running query...');
    console.log('SELECT * FROM players WHERE username = $1', username);
    return await pool.query(query);    
}