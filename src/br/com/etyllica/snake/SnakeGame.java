package br.com.etyllica.snake;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.com.etyllica.core.context.Application;
import br.com.etyllica.core.context.UpdateIntervalListener;
import br.com.etyllica.core.event.KeyEvent;
import br.com.etyllica.core.graphics.Graphics;
import br.com.etyllica.core.linear.PointInt2D;

public class SnakeGame extends Application implements UpdateIntervalListener {

	//Game state
	boolean paused = true;
	boolean gameOver = false;

	//Direction
	boolean right = true;
	boolean left = false;
	boolean up = false;
	boolean down = false;

	int mapX = 0;
	int mapY = 38;
	int tileSize = 10;
	boolean[][] map = new boolean[40][32];

	boolean drawGrid = true;

	List<PointInt2D> foods = new ArrayList<PointInt2D>();
	List<PointInt2D> body = new ArrayList<PointInt2D>();

	public SnakeGame(int w, int h) {
		super(w, h);
	}

	@Override
	public void load() {
		resetGame();

		//Update each 800ms
		updateAtFixedRate(200, this);

		loading = 100;
	}
	
	private void resetGame() {
		createMap(map);
		foods.clear();
		foods.add(createFood(map));

		body.clear();
		//Start with 4 body parts
		body.add(new PointInt2D(16, 16));
		body.add(new PointInt2D(15, 16));
		body.add(new PointInt2D(14, 16));
		body.add(new PointInt2D(13, 16));

		//Start going right
		right = true;
	}

	private void createMap(boolean[][] map) {
		for (int i = 0; i < map[0].length; i++) {
			map[0][i] = true;
			map[map.length-1][i] = true;
		}

		for (int j = 0; j < map.length; j++) {
			map[j][0] = true;
			map[j][map[0].length-1] = true;
		}
	}

	@Override
	public void timeUpdate(long now) {
		if (gameOver || paused) {
			return;
		}
		moveBody(body);
		checkCollision(body, map, foods);
	}

	private void checkCollision(List<PointInt2D> body, boolean[][] map, List<PointInt2D> foods) {
		PointInt2D head = body.get(0);

		//Yes, it is [y][x]
		if (map[head.getY()][head.getX()]) {
			//Hitted the map
			gameOver = true;
		}

		for(int i = 1; i < body.size(); i++) {
			PointInt2D bodyPart = body.get(i);
			if (head.getX() == bodyPart.getX() 
					&& head.getY() == bodyPart.getY()) {
				//Hitted a body part
				gameOver = true;
			}
		}

		//Why?
		//Because when you add a fruit while iterating the FOR
		//You might get a concurrent modification
		for (int i = foods.size()-1; i >= 0; i--) {
			PointInt2D food = foods.get(i);
			
			if (head.getX() == food.getX() 
					&& head.getY() == food.getY()) {
				
				//Ate a food, add a new body part
				body.add(new PointInt2D(food.getX(), food.getY()));
				foods.remove(i);
			}
		}
		
		if (foods.isEmpty()) {
			foods.add(createFood(map));
		}
	}

	public void updateKeyboard(KeyEvent event) {
		
		if (event.isKeyUp(KeyEvent.VK_ENTER)) {
			if (gameOver) {
				resetGame();	
			}
			gameOver = false;
			paused = false;
		}
		
		if (event.isKeyDown(KeyEvent.VK_UP)) {
			if (!down) {
				up = true;
				down = false;
				left = false;
				right = false;
			}
		} else if(event.isKeyDown(KeyEvent.VK_DOWN)) {
			if (!up) {
				up = false;
				down = true;
				left = false;
				right = false;
			}
		} else if(event.isKeyDown(KeyEvent.VK_LEFT)) {
			if (!right) {
				up = false;
				down = false;
				left = true;
				right = false;	
			}
		}
		if (event.isKeyDown(KeyEvent.VK_RIGHT)) {
			if (!left) {
				up = false;
				down = false;
				left = false;
				right = true;	
			}
		}
	}

	private void moveBody(List<PointInt2D> body) {

		for (int i = body.size()-1; i > 0; i--) {
			PointInt2D part = body.get(i);
			PointInt2D nextPart = body.get(i-1);
			part.setX(nextPart.getX());
			part.setY(nextPart.getY());
		}

		PointInt2D head = body.get(0);

		if (right) {
			head.setX(head.getX()+1);
		} else if (left) {
			head.setX(head.getX()-1);
		} else if (up) {
			//Our axis is inverted
			head.setY(head.getY()-1);
		} else if (down) {
			//Our axis is inverted
			head.setY(head.getY()+1);
		}
	}

	@Override
	public void draw(Graphics g) {		
		drawMap(g, map);
		drawBody(g, body);

		for (PointInt2D food: foods) {
			drawTile(g, food.getX(), food.getY());
		}
		
		if(paused) {
			g.drawString("PRESS ENTER TO START", 90, 138);
		} else if (gameOver) {
			g.drawString(this, "GAME OVER");
			g.drawString("PRESS ENTER TO RESTART", 90, 138);
		}
	}

	private void drawBody(Graphics g, List<PointInt2D> body) {
		for (PointInt2D bodyPart : body) {
			drawTile(g, bodyPart.getX(), bodyPart.getY());
		}
	}

	private void drawMap(Graphics g, boolean[][] map) {
		for (int j = 0; j < map.length; j++) {
			for (int i = 0; i < map[0].length; i++) {

				if (map[j][i]) {
					g.setColor(Color.GRAY);
				} else {
					g.setColor(Color.GREEN);
				}
				drawTile(g, i, j);
			}
		}
	}

	private void drawTile(Graphics g, int x, int y) {
		g.fillRect(mapX+x*tileSize, mapY+y*tileSize, tileSize, tileSize);

		if(drawGrid) {
			g.setColor(Color.BLACK);
			g.drawRect(mapX+x*tileSize, mapY+y*tileSize, tileSize, tileSize);
		}
	}

	private PointInt2D createFood(boolean[][] map) {
		Random random = new Random();

		int x = random.nextInt(map[0].length);
		int y = random.nextInt(map.length);

		//Avoid map obstacles
		//Yes, it is [y][x]
		if (map[y][x]) {
			return createFood(map);
		}

		return new PointInt2D(x, y);
	}

}
