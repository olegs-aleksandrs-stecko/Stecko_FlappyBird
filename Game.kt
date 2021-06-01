//Olegs Aleksandrs Stecko, Vladislavs Pavlovs, Edvards Jansons

package com.mygdx.flappybird

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import kotlin.random.Random
import kotlin.math.max

class Game : ApplicationAdapter() {
    var alpha = 0f
    val g = -1f
    var V = 0f
    var Ypos = 0f
    var birdState = 1
    var frameCounter = 0
    var groundMovement = 0f
    var score = 0
    var bestScore = 0

    var width = 0f
    var height = 0f
    var menueWidth = 0f
    var menueHeight = 0f
    var size = 0f


    var batch: SpriteBatch? = null
    var background: Texture? = null
    var ground: Texture? = null
    var menue: Texture? = null
    var birds = mutableListOf<Texture>()
    var birdSprite: Sprite? = null
    var pipe: Texture? = null
    var pipes = arrayOf<Array<Float>>()
    var scoreTextures = mutableListOf<Texture>()
    var gameOver: Texture? = null
    var gameOverMenue: Texture? = null

    var isInMenue = true
    var dead = false
    var deathMenue = false

    var wing: Sound? = null
    var hit: Sound? = null
    var swoosh: Sound? = null

    override fun create() {
        varAssignment()
    }
    override fun render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch!!.begin()
        batch!!.draw(background, 0f, 0f, width, height)
        if(!isInMenue) {
            birdLogic()
            pipeLogic()
            gameCheck()
            caseDead()
        } else if(isInMenue)
            caseMenue()
        groundAnimation()
        numberLogic()
        batch!!.end()
    }
    override fun dispose() {
        batch!!.dispose()
        background!!.dispose()
        wing!!.dispose()
        hit!!.dispose()
        swoosh!!.dispose()
    }

    fun varAssignment(){
        batch = SpriteBatch()

        background = Texture("sprites/background-day.png")
        ground = Texture("sprites/base.png")
        menue = Texture("sprites/message.png")
        pipe = Texture("sprites/pipe-green.png")
        gameOver = Texture("sprites/gameover.png")
        gameOverMenue = Texture("sprites/scores.png")

        wing = Gdx.audio.newSound(Gdx.files.internal("audio/wing.ogg"))
        hit = Gdx.audio.newSound(Gdx.files.internal("audio/hit.ogg"))
        swoosh = Gdx.audio.newSound(Gdx.files.internal("audio/swoosh.ogg"))

        width = Gdx.graphics.width.toFloat()
        height = Gdx.graphics.height.toFloat()
        menueWidth = width/2
        menueHeight = height/2
        size = width/10
        Ypos = Gdx.graphics.height.toFloat() / 2
        for(i in 0..2){
            var arrayTemp = arrayOf<Float>()
            for(j in 0..1){
                arrayTemp += -0f
            }
            pipes += arrayTemp
        }


        try {
            val file = Gdx.files.local("bestScore.txt")
            bestScore = max(bestScore,file.readString().toInt())
        }catch(e: Exception){
            print("Something wrong with bestScore file.\n")
        }
        val color = Random.nextInt(0,2)
        when (color){
            0 -> setBird("blue")
            1 -> setBird("red")
            2 -> setBird("yellow")
        }

        for(i in 0..9){
            scoreTextures.add(Texture("sprites/${i}.png"))
        }
    }

    fun setBird(color: String) {
        val first = "sprites/" + color + "bird-downflap.png"
        val second = "sprites/" + color + "bird-midflap.png"
        val thrid = "sprites/" + color + "bird-upflap.png"
        birds.add(Texture(first))
        birds.add(Texture(second))
        birds.add(Texture(thrid))
        birds.add(Texture(second))
    }

    fun pipeLogic(){
        if (pipes[0][0] <= -width / 5) {
            pipes[0][0] = pipes[1][0]
            pipes[0][1] = pipes[1][1]
            pipes[1][0] = pipes[2][0]
            pipes[1][1] = pipes[2][1]
            pipes[2][0] = (width * 1.6).toFloat()
            pipes[2][1] = Random.nextFloat()
            while (pipes[2][1] < 0.25f || pipes[2][1] > 0.9f)
                pipes[2][1] = Random.nextFloat()
        }

        for (i in 0..2) {
            batch!!.draw(pipe, pipes[i][0], -height / 10, width / 10, height * pipes[i][1])
            batch!!.draw(pipe, pipes[i][0], height + height / 10, width / 10, -height + height * pipes[i][1])
            if (!dead)
                pipes[i][0] -= 5f
        }
    }

    fun gameCheck(){
        if(!dead) {
            for (i in 0..1) {
                if (pipes[i][0] <= (width + size - 20) / 2 && pipes[i][0] + width / 10 >= (width - size - 20) / 2) {
                    if (!((height * pipes[i][1]) - height / 10 <= (Ypos ) && (-height + height * pipes[i][1]) + height * 1.1 >= (Ypos + size))) {
                        dead = true
                        hit!!.play(1f)
                        bestScore = max(score, bestScore)
                        val file = Gdx.files.local("bestScore.txt")
                        file.writeString("$bestScore", false)
                        break
                    }
                } else if (Ypos - size / 2 <= height / 10) {
                    dead = true
                    bestScore = max(score, bestScore)
                    val file = Gdx.files.local("bestScore.txt")
                    file.writeString("$bestScore", false)
                    break
                }
                if (((pipes[i][0] + width / 10) - 4) <= (width - size) / 2 && (pipes[i][0] + width / 10) >= (width - size) / 2) {
                    swoosh!!.play(1f)
                    score++
                }
            }
        }
        if (score % 100 == 0 && score != 0){
            when ((score / 100) % 2) {
                0-> {background = Texture("sprites/background-day.png")
                    pipe = Texture("sprites/pipe-green.png")}
                1-> {background = Texture("sprites/background-night.png")
                    pipe = Texture("sprites/pipe-red.png")}
            }
        }
    }

    fun groundAnimation(){
        if(!dead&&!isInMenue){
            groundMovement -= 5
            if (groundMovement <= -width * 1.9)
                groundMovement = 0f
        }
        batch!!.draw(ground, groundMovement, 0f,width*3, height/10)
    }

    fun birdLogic(){
        birdSprite = Sprite(birds[birdState])
        birdSprite!!.setPosition((width - size) / 2, Ypos)
        birdSprite!!.setSize(size, size)
        birdSprite!!.rotation = alpha
        birdSprite!!.draw(batch)
        if(!dead) {
            //speed counting
            V += g
            if (Gdx.input.justTouched() && !dead) {
                V = 20f
                wing!!.play(1f)
            }
            Ypos += V

            //rotation counting
            if (V > 0 && alpha < 50f)
                alpha += 2
            else if (V < 0 && alpha > -50f)
                alpha -= 2

            //animation
            if (frameCounter == 4) {
                when (birdState) {
                    0 -> birdState = 1
                    1 -> birdState = 2
                    2 -> birdState = 3
                    3 -> birdState = 0
                }
                frameCounter = 0
            }
            frameCounter++
        }
    }

    fun caseDead(){
        if (dead && !deathMenue){
            if (Ypos - size/2 <= height/10){
                hit!!.play(1f)
                deathMenue = true
            }else{
                V += g
                Ypos += V
                if(alpha > - 90f)
                    alpha -= 2
            }

        } else if(deathMenue){
            batch!!.draw(gameOver, width*0.1f, height*0.7f, width*0.8f, height*0.1f)
            batch!!.draw(gameOverMenue, width*0.1f, height*0.4f, width*0.8f, height*0.25f)
            displayNumbers(score, width*0.19f, height*0.53f)
            displayNumbers(bestScore, width*0.19f, height*0.45f)
            if (Gdx.input.justTouched()) {
                dead = false
                isInMenue = true
                deathMenue = false
                V = 0f
                Ypos = Gdx.graphics.height.toFloat() / 2
                alpha = 0f
                score = 0
            }
        }
    }

    fun caseMenue(){
        batch!!.draw(menue, (width - menueWidth) /  2, (height - menueHeight) / 2, menueWidth, menueHeight)
        if (Gdx.input.justTouched()){
            isInMenue = false
            var distance = width*1.2f
            for(i in 0..2){
                pipes[i][0] = distance
                distance += width*0.6f
                pipes[i][1] = Random.nextFloat()
                while (pipes[i][1]<0.25f || pipes[i][1] > 0.8f)
                    pipes[i][1] = Random.nextFloat()
            }
        }
    }

    fun numberLogic(){
        if(!isInMenue && !dead)
            displayNumbers(score, width*0.1f, height - width*0.2f)
    }
    fun displayNumbers(number:Int, x:Float, y:Float){
        val scoreDigits = mutableListOf<Int>()
        var output = number
        if (output == 0) {
            batch!!.draw(scoreTextures[0], x, y, size, size)
        } else {
            while (output > 0) {
                scoreDigits.add(output % 10)
                output /= 10
            }
            scoreDigits.reverse()
            Gdx.app.log("scoreDigits", "$scoreDigits")
            batch!!.draw(scoreTextures[scoreDigits[0]], x , y, size, size)
            for (i in 1 until scoreDigits.count()) {
                batch!!.draw(scoreTextures[scoreDigits[i]], (size*i)+x, y, size, size)
            }
        }
    }
}