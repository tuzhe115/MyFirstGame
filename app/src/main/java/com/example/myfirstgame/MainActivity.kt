package com.example.myfirstgame

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Button
import android.content.Intent
import android.content.SharedPreferences

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import android.content.ContentValues
import android.database.Cursor

import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    // Source:https://developer.android.com/kotlin/common-patterns

    // Initialize variables for player names for latter functions
    private lateinit var player1Name: String
    private lateinit var player2Name: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Store references to widgets in variables from base code
        turnTextView = findViewById(R.id.turnTextView) as TextView
        tableLayout = findViewById(R.id.table_layout) as TableLayout
        resetButton = findViewById(R.id.button) as Button
        // Add startNewGame function to reset button from base code
        resetButton!!.setOnClickListener() {startNewGame(false)}

        // Set up score button to score page
        /*
        val scoreButton = findViewById<Button>(R.id.viewScoreButton)

        scoreButton.setOnClickListener {
            val intent = Intent(this, ScoreActivity::class.java)
            startActivity(intent)
        }*/

        // Get player names from shared preferences
        val sharedPreferences: SharedPreferences = getSharedPreferences("PlayerData", MODE_PRIVATE)
        // Store player names as default values if player names are empty
        player1Name = sharedPreferences.getString("player1Name", "Player 1") ?: "Player 1"
        player2Name = sharedPreferences.getString("player2Name", "Player 2") ?: "Player 2"

        val currentPlayerTextView = findViewById<TextView>(R.id.currentPlayerTextView)
        // Update current player text
        updateCurrentPlayerText(currentPlayerTextView)

        // Set up bottom navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        setupBottomNavigation(this, bottomNavigationView)

        // Start new game from base code
        startNewGame(true)

    }

    // Initialize variables from base code
    var gameBoard: Array<CharArray> = Array(4) { CharArray(4) }
    var turn = 'X'
    var turnTextView: TextView? = null
    var tableLayout: TableLayout? = null
    var resetButton: Button? = null

    // Function for starting new game from base code
    private fun startNewGame(setClickListener: Boolean) {
        val currentPlayerTextView = findViewById<TextView>(R.id.currentPlayerTextView)
        turn = 'X'
        turnTextView?.text =
                String.format(resources.getString(R.string.turn), turn)
        // Update current player text
        updateCurrentPlayerText(currentPlayerTextView)
        // Initialize game board and cells. Set click listeners for cells to be cellClickListener from base code
        for (i in 0 until gameBoard.size) {
            for (j in 0 until gameBoard[i].size) {
                gameBoard[i][j] = ' '
                val cell = (tableLayout?.getChildAt(i) as
                            android.widget.TableRow).getChildAt(j) as TextView
                cell.text = ""
                if (setClickListener) {
                    cell.setOnClickListener { cellClickListener(i,j)}
                }
            }
        }
    }

    // Function for updating current player text
    private fun updateCurrentPlayerText(textView: TextView) {
        val currentPlayerName = if (turn == 'X') player1Name else player2Name
        textView.text = getString(R.string.current_player, currentPlayerName)
    }

    // Function for cell click listener from base code
    private fun cellClickListener(row: Int, column: Int) {
        val currentPlayerTextView = findViewById<TextView>(R.id.currentPlayerTextView)
        // Check if cell is empty
        if (gameBoard[row][column] == ' ') {

            // Set cell to X or O and update game board
            gameBoard[row][column] = turn

            // Update cell text
            ((tableLayout?.getChildAt(row) as android.widget.TableRow).getChildAt(column) as
                    TextView).text = turn.toString()

            // Exchange turn
            turn = if ('X' == turn) 'O' else 'X'

            updateCurrentPlayerText(currentPlayerTextView)


            // Update turn text
            turnTextView?.text =
                    String.format(resources.getString(R.string.turn), turn)
            // Check game status
            checkGameStatus()
        }
    }

    // Function for checking if board is full from base code
    private fun isBoardFull (gameBoard:Array<CharArray>): Boolean {
        // Check if there is any cell empty
        for (i in 0 until gameBoard.size) {
            for (j in 0 until gameBoard[i].size) {
                if (gameBoard[i][j] == ' ') {
                    return false
                }
            }
        }
        return true
    }

    // Function for checking if there is a winner. Update for 4x4 grid.
    private fun isWinner (gameBoard:Array<CharArray>, w: Char): Boolean {
        for (i in 0 until gameBoard.size) {

            // Check for horizontal winning condition for 4x4 grid
            if (gameBoard[i][0] == w && gameBoard[i][1] == w && gameBoard[i][2] == w && gameBoard[i][3] == w) {
                return true
            }

            // Check for vertical winning condition for 4x4 grid

            if (gameBoard[0][i] == w && gameBoard[1][i] == w && gameBoard[2][i] == w && gameBoard[3][i] == w) {
                return true
            }
        }

        // Check for diagonal winning condition for 4x4 grid
        if ((gameBoard[0][0] == w && gameBoard[1][1] == w && gameBoard[2][2] == w && gameBoard[3][3] == w) ||
            (gameBoard[0][3] == w && gameBoard[1][2] == w && gameBoard[2][1] == w && gameBoard[3][0] == w)){
            return true
        }
        return false
    }

    // Function for checking game status from base code.
    private fun checkGameStatus() {
        var state: String? = null
        val database = ScoreDb(this)
        // Update game status and database
        if (isWinner(gameBoard, 'X')) {
            state  = String.format(resources.getString(R.string.winner), player1Name)

            // Update database with player1 winning and player2 losing
            database.addOrUpdatePlayer(player1Name, isWin = true, isDraw = false)
            database.addOrUpdatePlayer(player2Name, isWin = false, isDraw = false)

        } else if (isWinner(gameBoard, 'O')) {
            state  = String.format(resources.getString(R.string.winner), player2Name)

            // Update database with player1 losing and player2 winning
            database.addOrUpdatePlayer(player2Name, isWin = true, isDraw = false)
            database.addOrUpdatePlayer(player1Name, isWin = false, isDraw = false)
        } else {
            if (isBoardFull(gameBoard)) {
                state = resources.getString(R.string.draw)

                // Update database with player1 drawing and player2 drawing
                database.addOrUpdatePlayer(player1Name, isWin = false, isDraw = true)
                database.addOrUpdatePlayer(player2Name, isWin = false, isDraw = true)
            }
        }

        // Show game status in alert dialog if game status is not empty
        if (state != null) {
            turnTextView?.text = state
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setMessage(state)
            builder.setPositiveButton(android.R.string.ok, {dialog, id -> startNewGame(false)
            })
            val dialog = builder.create()
            dialog.show()
        }
    }

    // Source: https://developer.android.com/training/data-storage/sqlite
    // Set up database for storing players' score
    class ScoreDb(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

        // Source: https://medium.com/@appdevinsights/companion-object-in-kotlin-c3a1203cd63c
        // Initialize the variables for the database in companion object
        companion object {
            const val DB_VERSION = 1
            const val DB_NAME = "Score.db"
            const val TABLE_NAME = "Score"
            const val COLUMN_PLAYER_NAME = "player_name"
            const val COLUMN_WINS = "wins"
            const val COLUMN_DRAWS = "draws"
            const val COLUMN_LOSSES = "losses"
        }

        override fun onCreate(db: SQLiteDatabase) {

            // Create table for storing players' score
            val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PLAYER_NAME TEXT NOT NULL,
                $COLUMN_WINS INTEGER DEFAULT 0,
                $COLUMN_DRAWS INTEGER DEFAULT 0,
                $COLUMN_LOSSES INTEGER DEFAULT 0
            )
        """
            db.execSQL(createTableQuery)
        }

        // Function for upgrading database version
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
        }

        // Source: https://developer.android.com/reference/android/database/Cursor
        // Function for adding new player or updating existing players' score
        fun addOrUpdatePlayer(playerName: String, isWin: Boolean, isDraw: Boolean) {
            val db = writableDatabase
            val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_PLAYER_NAME = ?", arrayOf(playerName))

            // Check if player exists and update score
            if (cursor.moveToFirst()) {
                // Update existing player
                val currentWins = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WINS))
                val currentDraws = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DRAWS))
                val currentLosses = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LOSSES))

                val contentValues = ContentValues()
                if (isWin) {
                    contentValues.put(COLUMN_WINS, currentWins + 1)
                } else if (isDraw) {
                    contentValues.put(COLUMN_DRAWS, currentDraws + 1)
                } else {
                    contentValues.put(COLUMN_LOSSES, currentLosses + 1)
                }

                db.update(TABLE_NAME, contentValues, "$COLUMN_PLAYER_NAME = ?", arrayOf(playerName))
            } else {
                // Insert new player
                val contentValues = ContentValues().apply {
                    put(COLUMN_PLAYER_NAME, playerName)
                    put(COLUMN_WINS, if (isWin) 1 else 0)
                    put(COLUMN_DRAWS, if (isDraw) 1 else 0)
                    put(COLUMN_LOSSES, if (!isWin && !isDraw) 1 else 0)
                }
                db.insert(TABLE_NAME, null, contentValues)
            }
            cursor.close()
            db.close()
        }

        // Function for getting score from the database
        fun getScore(): List<Map<String, Any>> {
            val db = readableDatabase
            // Variable for storing score
            val score = mutableListOf<Map<String, Any>>()
            val cursor = db.query(
                TABLE_NAME, null, null, null, null, null,
                "$COLUMN_WINS DESC, $COLUMN_PLAYER_NAME ASC"
            )

            while (cursor.moveToNext()) {
                val playerName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLAYER_NAME))
                val wins = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WINS))
                val draws = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DRAWS))
                val losses = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LOSSES))

                // Storing scores
                score.add(
                    mapOf(
                        "playerName" to playerName,
                        "wins" to wins,
                        "draws" to draws,
                        "losses" to losses
                    )
                )
            }
            cursor.close()
            db.close()
            return score
        }

        // Function for deleting player data
        fun deletePlayer(playerName: String) {
            val db = writableDatabase
            db.delete(TABLE_NAME, "$COLUMN_PLAYER_NAME = ?", arrayOf(playerName))
            db.close()
        }


    }

    // Function for setting up bottom navigation
    fun setupBottomNavigation(activity: AppCompatActivity, bottomNavigationView: BottomNavigationView) {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // Do nothing when clicking home
                R.id.nav_home -> {
                    true
                }
                // Go to score page
                R.id.nav_score -> {
                    if (activity !is ScoreActivity) {
                        val intent = Intent(activity, ScoreActivity::class.java)
                        activity.startActivity(intent)
                    }
                    true
                }
                // Go to register page
                R.id.nav_register -> {
                    if (activity !is RegisterActivity) {
                        val intent = Intent(activity, RegisterActivity::class.java)
                        activity.startActivity(intent)
                    }
                    true
                }
                else -> false
            }
        }
    }


}