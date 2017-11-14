
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

@SuppressWarnings("serial")
public class GameFrame extends JFrame implements ActionListener{

	private GamePanel gamePanel = new GamePanel();
	private JButton startButton = new JButton("Start");
	private boolean running = false;
	private boolean paused = false;

	final int GRID_EL_SIZE = 20;

    public GameFrame(){
    	Container cp = getContentPane();
    	cp.setLayout(new BorderLayout());
        JPanel p = new JPanel();
      	p.setLayout(new GridLayout(1,2));
      	p.add(startButton);

        setSize(800, 600);
        setVisible(true);
        setResizable(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        cp.add(gamePanel, BorderLayout.CENTER);
        cp.add(p, BorderLayout.SOUTH);

        startButton.addActionListener(this);

        this.getRootPane().addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                gamePanel.updatePanelSize();
            }
        });

    }

    public static void main(String[] args){
    	GameFrame gameFrame = new GameFrame();
    	gameFrame.setVisible(true);
    }

    public void actionPerformed(ActionEvent e){
    	Object s = e.getSource();
		if (s == startButton)
		{	

			running = !running;
			if (running){
				gamePanel.startGame();
				startButton.setText("Stop");
				gamePanel.requestFocus();
				runGameLoop();
			}
			else {
				startButton.setText("Start");
			}
		}
	}

	public void runGameLoop(){
      
      Thread loop = new Thread(){
        public void run(){
        	gameLoop();
        }
      };
      loop.start();
   	}

	private void gameLoop(){

		double lastLoopTime = System.nanoTime();
   		final int TARGET_FPS = 15;
   		final double OPTIMAL_TIME = 1000000000 / TARGET_FPS;

		// update the frame counter

		while(running){

			double now = System.nanoTime();
			double updateLength = now - lastLoopTime;
			lastLoopTime = now;
			double delta = updateLength / ((double)OPTIMAL_TIME);

			// update the game logic
			gameUpdates();

			// draw everyting
			gameRender();

			try{
				Thread.sleep( (long)(lastLoopTime - System.nanoTime() + OPTIMAL_TIME)/(long)1000000 );
			} catch(Exception e) {	
				System.out.println("Thread sleep error");
			}

		}
	}

	private void gameUpdates(){
		//System.out.println("okok");
		gamePanel.update();
		gamePanel.collition();
	}

	private void gameRender(){
		//System.out.println("jebb");
		gamePanel.repaint();
	}

	private class GamePanel extends JPanel implements KeyListener{

		private Snek snake;
		private Frut fruit;

		public GamePanel(){
	        this.setFocusable(true);
	        this.addKeyListener(this);
		}

	    public void update(){
	    	snake.update();
	    	collition();
	    }

		@Override
	    public void paint(Graphics g) {
	        super.paintComponent(g);
	        if(snake != null) snake.drawSnek(g);
	        if(fruit != null) fruit.drawFrut(g);
		}

		public void collition(){
			if(snake.x == fruit.x && snake.y == fruit.y){
	    		snake.grow();
	    		fruit.pickLocation(snake.body);
	    	}
	    	if(snake.x < 0 || snake.x >= this.getWidth() - 20 || snake.y < 0 || snake.y >= this.getHeight() - 20){
	    		startButton.setText("Start");
	    		running = false;
	    	}
		}

		public void keyPressed(KeyEvent e) {
	        Integer key = e.getKeyCode();
	        if (key == KeyEvent.VK_LEFT) {
	        	snake.moveLeft();
	        }
	        if (key == KeyEvent.VK_RIGHT) {
	        	snake.moveRight();
	        }
	        if (key == KeyEvent.VK_UP) {
	        	snake.moveUp();
	        }
	        if (key == KeyEvent.VK_DOWN) {
	        	snake.moveDown();
	        }
	    }

	    public void startGame(){
	    	snake = new Snek(20, 20, 20, 0);
	    	fruit = new Frut(this.getWidth(), this.getHeight(), 20, snake.body);
	    }

	    public void updatePanelSize(){
	        this.fruit.width = this.getWidth();
	        this.fruit.height = this.getHeight();
	    }

	    public void keyReleased(KeyEvent e) {

	    }


	    public void keyTyped(KeyEvent e) {

	    }
	}


	private class Snek {
		
		private int x;
		private int y;
		private int xSpeed;
		private int ySpeed;
		private boolean alive;
		private boolean[] movement = {false, false, false, false};
		//							   left, right, up   , down
		//private Stack body = new Stack();
		private Queue<Integer[]> body = new LinkedList<Integer[]>();
		//private Iterator<E> iter = body.iterator();

		public Snek(int x, int y, int xS, int yS) {
			this.x = x;
			this.y = y;
			this.xSpeed = xS;
			this.ySpeed = yS;
			this.alive = true;

		}

		public void moveLeft(){
			if(this.xSpeed == 0){
				movement[0] = true;
				movement[1] = false;
				movement[2] = false;
				movement[3] = false;
			}
		}
		public void moveRight(){
			if(this.xSpeed == 0){
				movement[0] = false;
				movement[1] = true;
				movement[2] = false;
				movement[3] = false;
			}
		}
		public void moveUp(){
			if(this.ySpeed == 0){
				movement[0] = false;
				movement[1] = false;
				movement[2] = true;
				movement[3] = false;
			}
		}
		public void moveDown(){
			if(this.ySpeed == 0){
				movement[0] = false;
				movement[1] = false;
				movement[3] = true;
				movement[2] = false;
			}
		}

		public void update(){
			this.moveSnek();
			this.x += this.xSpeed;
			this.y += this.ySpeed;
			this.collitionSelf();
			this.body.add(new Integer[]{this.x, this.y});
			
		}

		public void drawSnek(Graphics g) {
			g.fillRect(this.x, this.y, 20, 20);
			for (Integer[] e : this.body) {
				g.fillRect(e[0], e[1], 20, 20);
			}
			this.body.poll();
		}

		public void grow(){
			this.body.add(new Integer[]{this.x, this.y});
		}

		public void moveSnek(){
			if(this.movement[0]) this.setSpeed(-20, 0);
			if(this.movement[1]) this.setSpeed(20, 0);
			if(this.movement[2]) this.setSpeed(0, -20);
			if(this.movement[3]) this.setSpeed(0, 20);
		}

		public void setSpeed(int x, int y){
			this.xSpeed = x;
			this.ySpeed = y;
		}

		public void collitionSelf(){
			for (Integer[] e : this.body) {
				if(e[0] == this.x && e[1] == this.y){
					alive = false;
					startButton.setText("Start");
					running = false;
					break;
				} 
			}
		}

		public void restartSnek(){
			this.body.clear();
			this.x = 20; this.y = 20;
			this.xSpeed = 20; this.ySpeed = 0;
		}
	}

	private class Frut {
		private int x;
		private int y;
		private int width;
		private int height;
		private int size;


		public Frut(int width, int height, int size, Queue<Integer[]> body){
			this.width = width;
			this.height = height;
			this.size = size;
			this.pickLocation(body);
		}

		public void drawFrut(Graphics g){
			g.fillRect(this.x, this.y, 20, 20);
		}

		public void pickLocation(Queue<Integer[]> body){
			this.x = (int)(Math.random()*(this.width/this.size))*this.size;
			this.y = (int)(Math.random()*(this.height/this.size))*this.size;
			//this.validateLocation(body);
		}

		public void validateLocation(Queue<Integer[]> body){
			for (Integer[] e: body){
				if(e[0] == this.x && e[1] == this.y){
					pickLocation(body);
					break;
				}
			}
		}
	}
}