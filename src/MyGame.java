

import br.com.etyllica.Etyllica;
import br.com.etyllica.core.context.Application;
import br.com.etyllica.snake.SnakeGame;

public class MyGame extends Etyllica {

	private static final long serialVersionUID = 1L;

	public MyGame() {
		super(320, 440);
	}

	public static void main(String[] args) {
		MyGame app = new MyGame();
		app.setTitle("My Game");
		app.init();
	}
	
	@Override
	public Application startApplication() {
		return new SnakeGame(w, h);
	}

}
