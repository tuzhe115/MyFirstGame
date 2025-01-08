package com.example.myfirstgame

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TableLayout
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        turnTextView = findViewById(R.id.turnTextView) as TextView
        tableLayout = findViewById(R.id.table_layout) as TableLayout
        resetButton = findViewById(R.id.button) as android.widget.Button
        resetButton!!.setOnClickListener() {startNewGame(false)}
        startNewGame(true)
    }

    var gameBoard: Array<CharArray> = Array(4) { CharArray(4) }
    var turn = 'X'
    var turnTextView: TextView? = null
    var tableLayout: TableLayout? = null
    var resetButton: android.widget.Button? = null

    private fun startNewGame(setClickListener: Boolean) {
        turn = 'X'
        turnTextView?.text =
                String.format(resources.getString(R.string.turn), turn)
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

    private fun cellClickListener(row: Int, column: Int) {
        if (gameBoard[row][column] == ' ') {
            gameBoard[row][column] = turn

            ((tableLayout?.getChildAt(row) as android.widget.TableRow).getChildAt(column) as
                    TextView).text = turn.toString()

            turn = if ('X' == turn) 'O' else 'X'
            turnTextView?.text =
                    String.format(resources.getString(R.string.turn), turn)
            checkGameStatus()
        }
    }

    private fun isBoardFull (gameBoard:Array<CharArray>): Boolean {
        for (i in 0 until gameBoard.size) {
            for (j in 0 until gameBoard[i].size) {
                if (gameBoard[i][j] == ' ') {
                    return false
                }
            }
        }
        return true
    }

    private fun isWinner (gameBoard:Array<CharArray>, w: Char): Boolean {
        for (i in 0 until gameBoard.size) {
            if (gameBoard[i][0] == w && gameBoard[i][1] == w && gameBoard[i][2] == w && gameBoard[i][3] == w) {
                return true
            }

            if (gameBoard[0][i] == w && gameBoard[1][i] == w && gameBoard[2][i] == w && gameBoard[3][i] == w) {
                return true
            }
        }

        if ((gameBoard[0][0] == w && gameBoard[1][1] == w && gameBoard[2][2] == w && gameBoard[3][3] == w) ||
            (gameBoard[0][3] == w && gameBoard[1][2] == w && gameBoard[2][1] == w && gameBoard[3][0] == w)){
            return true
        }
        return false
    }

    private fun checkGameStatus() {
        var state: String? = null
        if (isWinner(gameBoard, 'X')) {
            state  = String.format(resources.getString(R.string.winner), 'X')
        } else if (isWinner(gameBoard, 'O')) {
            state  = String.format(resources.getString(R.string.winner), 'O')
        } else {
            if (isBoardFull(gameBoard)) {
                state = resources.getString(R.string.draw)
            }
        }

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
}