package com.tpcstld.jetris;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

public class JetrisMainView extends View {

	Paint paint = new Paint();

	// Internal Options
	static int test = 0;
	static int numSquaresX = 16; // Total number of columns
	static int numSquaresY = 22; // total number of rows
	static double textScaleSize = 0.8; // Text scaling

	// External Options
	static double defaultGravity = 0.05; // The default gravity of the game
	static int FPS = 1000 / 30; // Frames per second of the game (1000/given
								// value)
	static int flickSensitivity = 30; // How sensitive is the flick gesture
	static int slackLength = 1000; // How long the stack goes on for in
									// milliseconds
	static double softDropMultipler = 9; // How fast soft dropping is

	static Timer time = new Timer(true);
	static boolean slack = false; // Whether or not slack is currently active
	static boolean pause = false; // Whether or not the pause is currently
									// paused
	static boolean lose = false; // Whether or not the game is still in progress
	static boolean holdOnce = false; // Whether or not the block has already
										// been held once
	static boolean hardDrop = false; // Whether or not harddropping is active
	static boolean slackOnce = false; // Whether or not slack as already been
										// activated
	static boolean turnSuccess = false; // Whether or not a turn was successful
	static boolean softDrop = false; // Whether or not softdropping is active
	static boolean kick = false; // Whether or not the block was kicked to
									// location
	static boolean difficult = false; // Whether or not a clear was considered
										// to be "hard"
	static boolean lastDifficult = false; // Whether or not the last clear was
											// considered to be "hard"
	static int score = 0;
	static int mainFieldShiftX; // How much the screen is shifted to the right
	static int mainFieldShiftY; // How much the screen is shifted downwards
	static int squareSide; // The size of one square
	static int numberOfBlocksWidth = 10; // The number of columns of blocks in
											// the main field
	static int numberOfBlocksLength = 22; // The number of rows of blocks in the
											// main field
	static int numberOfHoldShapeWidth = 4; // The width of the auxiliary boxes
	static int numberOfHoldShapeLength = 2; // The length of the auxiliary boxes
	static int holdShapeXStarting; // Where the hold box starts (x)
	static int nextShapeYStarting; // Where the first next box starts (y)
	static int nextShapeY2Starting; // Where the second next box starts (y)
	static int nextShapeY3Starting; // Where the third next box starts (y)
	static int thisShape = -1; // The NEXT shape on the playing field.
	static int nextShape = -1; // The NEXT2 shape on the playing field.
	static int nextShape1 = -1; // The NEXT3 shaped on the playing field.
	static int holdShape = -1; // The shape currently being held.
	static int lastShape = -1; // The CURRENT shape on the playing field.
	static int pickShapeCounter = 0; // Number of blocks picked from the current
										// bag
	static int shape = 0; // The current shape of the block
	static int combo = 0; // The current combo
	static int scoreInfoYStarting; // Where the score box starts (y)
	static Random r = new Random(); // The randomizer
	// Array detailing the type of block in each square
	static int[][] blocks = new int[numberOfBlocksWidth][numberOfBlocksLength];
	// Array detailing the colour in each square
	static int[][] colors = new int[numberOfBlocksWidth][numberOfBlocksLength];
	// Array displaying the block in hold
	static int[][] holdBlocks = new int[numberOfHoldShapeWidth][numberOfHoldShapeLength];
	// Array displaying the next block
	static int[][] nextBlocks = new int[numberOfHoldShapeWidth][numberOfHoldShapeLength];
	// Array displaying the next2 block
	static int[][] next2Blocks = new int[numberOfHoldShapeWidth][numberOfHoldShapeLength];
	// Array displaying the next3 block
	static int[][] next3Blocks = new int[numberOfHoldShapeWidth][numberOfHoldShapeLength];
	static int[] playLocationX = new int[4]; // X-coords of the blocks in play
	static int[] playLocationY = new int[4]; // Y-coords of the blocks in play
	static int[] timesShapeDrawn = new int[7]; // How many times a block has
												// been drawn in a bag
	static double gravity = defaultGravity; // The current gravity of the game
	static double totalGrav = 0; // The current gravity ticker
	static long clock = System.currentTimeMillis(); // Tracks the real time for
													// fps
	static boolean getScreenSize = false; // Initial getting screen size
											// variable

	// Blocks Data:
	// 0 = empty space
	// 1 = active space
	// 2 = locked space
	// 3 = ghost space

	public JetrisMainView(Context context) {
		super(context);

		Properties configFile = new Properties();
		try {
			configFile.load(new FileInputStream("config.properties"));
			defaultGravity = Double.parseDouble(configFile
					.getProperty("defaultGravity"));
			FPS = 1000 / Integer.parseInt(configFile.getProperty("FPS"));
			flickSensitivity = Integer.parseInt(configFile
					.getProperty("flickSenstivity"));
			slackLength = Integer.parseInt(configFile
					.getProperty("slackLength"));
			softDropMultipler = Integer.parseInt(configFile
					.getProperty("softDropMultipler"));
		} catch (FileNotFoundException e) {
			System.out.println("Missing config.properties. Using defaults.");
		} catch (IOException e) {
			e.printStackTrace();
		}

		setOnTouchListener(new OnTouchListener() {
			float x;
			float y;
			float prevX;
			float prevY;
			boolean turn;
			boolean hardDropped;
			float startingX;
			float startingY;

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (!pause & !lose) {
					switch (arg1.getAction()) {

					case MotionEvent.ACTION_DOWN:
						x = arg1.getX();
						y = arg1.getY();
						startingX = x;
						startingY = y;
						prevX = x;
						prevY = y;
						turn = true;
						hardDropped = false;
						return true;
					case MotionEvent.ACTION_MOVE:
						x = arg1.getX();
						y = arg1.getY();
						float dy = y - prevY;
						if (dy > flickSensitivity & !hardDropped) {
							hardDrop = true;
							slack = false;
							gravity = 20.0;
							turn = false;
							hardDropped = true;
						} else if (dy < -flickSensitivity) {
							if (!holdOnce) {
								holdShape();
							}
							turn = false;
						} else if (x - startingX > squareSide) {
							startingX = x;
							moveRight();
							turn = false;
						} else if (x - startingX < -squareSide) {
							startingX = x;
							moveLeft();
							turn = false;
						} else if (y - startingY > squareSide & !hardDropped) {
							gravity = defaultGravity * softDropMultipler;
							softDrop = true;
							turn = false;
						}
						prevX = x;
						prevY = y;
						coloring();
						return true;
					case MotionEvent.ACTION_UP:
						x = arg1.getX();
						y = arg1.getY();

						if (turn) {
							if (x < squareSide * numberOfBlocksWidth * 0.5) {
								shapeTurnCC();
							} else {
								shapeTurn();
							}
						}
						if (softDrop) {
							gravity = defaultGravity;
							softDrop = false;
						}
						coloring();
						return true;
					}
				}
				return false;
			}

		});
		newGame();
	}

	@Override
	public void onDraw(Canvas canvas) {

		if (!getScreenSize) {
			int width = this.getMeasuredWidth();
			int height = this.getMeasuredHeight();
			System.out.println(width + " " + height);
			squareSide = (int) Math.min(width / numSquaresX, height
					/ numSquaresY);
			holdShapeXStarting = squareSide * (numberOfBlocksWidth + 1);
			nextShapeYStarting = squareSide * 5;
			nextShapeY2Starting = squareSide * 8;
			nextShapeY3Starting = squareSide * 11;
			scoreInfoYStarting = squareSide * 3;
			mainFieldShiftX = squareSide;
			mainFieldShiftY = squareSide;
			getScreenSize = true;
			paint.setTextSize((float) (squareSide * textScaleSize));
		}
		paint.setColor(Color.BLACK);
		canvas.drawText("Score: " + score, holdShapeXStarting + mainFieldShiftX
				- squareSide, scoreInfoYStarting + mainFieldShiftY, paint);

		// Gravity falling mechanic.
		// totalGrav is incremented by gravity every tick.
		// when totalGrav is higher than 1, the shape moves down 1 block.
		if (!lose & !pause) {
			if (thisShape >= 0) {
				long temp = System.currentTimeMillis();
				long dtime = temp - clock;
				if (dtime > FPS) {
					totalGrav = totalGrav + gravity;
					clock = clock + FPS;
				}
				while (totalGrav >= 1) {
					shapeDown();
					totalGrav = totalGrav - 1;
				}
			}
		}
		coloring();
		ghostShape();

		for (int xx = mainFieldShiftX; xx <= squareSide * numberOfBlocksWidth
				+ mainFieldShiftX; xx += squareSide)
			canvas.drawLine(xx, mainFieldShiftY, xx, squareSide
					* (numberOfBlocksLength - 2) + mainFieldShiftY, paint);

		for (int xx = mainFieldShiftY; xx <= squareSide
				* (numberOfBlocksLength - 2) + mainFieldShiftY; xx += squareSide)
			canvas.drawLine(mainFieldShiftX, xx, squareSide
					* numberOfBlocksWidth + mainFieldShiftX, xx, paint);

		for (int x = 0; x < 8; x++) {
			// Setting the color of the blocks
			if (x == 0)
				paint.setColor(Color.CYAN);
			else if (x == 1)
				paint.setColor(Color.BLUE);
			else if (x == 2)
				paint.setColor(Color.DKGRAY);
			else if (x == 3)
				paint.setColor(Color.MAGENTA);
			else if (x == 4)
				paint.setColor(Color.GREEN);
			else if (x == 5)
				paint.setColor(Color.RED);
			else if (x == 6)
				paint.setColor(Color.YELLOW);
			else if (x == 7)
				paint.setColor(Color.GRAY);

			for (int xx = 0; xx < numberOfBlocksWidth; xx++)
				for (int yy = 2; yy < numberOfBlocksLength; yy++)
					if (blocks[xx][yy] != 0 & blocks[xx][yy] != 3
							& colors[xx][yy] == x) {
						canvas.drawRect(xx * squareSide + mainFieldShiftX,
								(yy - 2) * squareSide + mainFieldShiftY, xx
										* squareSide + squareSide
										+ mainFieldShiftX, (yy - 2)
										* squareSide + squareSide
										+ mainFieldShiftY, paint);
					}

			for (int xx = 0; xx < numberOfBlocksWidth; xx++)
				for (int yy = 0; yy < numberOfBlocksLength; yy++)
					if (blocks[xx][yy] == 3 & lastShape == x)
						canvas.drawCircle((float) (xx * squareSide + squareSide
								* 0.5 + mainFieldShiftX),
								(float) ((yy - 2) * squareSide + squareSide
										* 0.5 + mainFieldShiftY),
								(float) (squareSide * 0.5), paint);

			for (int xx = 0; xx < numberOfHoldShapeWidth; xx++)
				for (int yy = 0; yy < numberOfHoldShapeLength; yy++)
					if (holdBlocks[xx][yy] == 1 & holdShape == x)
						canvas.drawRect(xx * squareSide + holdShapeXStarting
								+ mainFieldShiftX, yy * squareSide
								+ mainFieldShiftY, xx * squareSide
								+ holdShapeXStarting + squareSide
								+ mainFieldShiftX, yy * squareSide + squareSide
								+ mainFieldShiftY, paint);

			for (int xx = 0; xx < numberOfHoldShapeWidth; xx++)
				for (int yy = 0; yy < numberOfHoldShapeLength; yy++)
					if (nextBlocks[xx][yy] == 1 & thisShape == x)
						canvas.drawRect(xx * squareSide + holdShapeXStarting
								+ mainFieldShiftX, yy * squareSide
								+ nextShapeYStarting + mainFieldShiftY, xx
								* squareSide + holdShapeXStarting + squareSide
								+ mainFieldShiftX, yy * squareSide
								+ nextShapeYStarting + squareSide
								+ mainFieldShiftY, paint);

			for (int xx = 0; xx < numberOfHoldShapeWidth; xx++)
				for (int yy = 0; yy < numberOfHoldShapeLength; yy++)
					if (next2Blocks[xx][yy] == 1 & nextShape == x)
						canvas.drawRect(xx * squareSide + holdShapeXStarting
								+ mainFieldShiftX, yy * squareSide
								+ nextShapeY2Starting + mainFieldShiftY, xx
								* squareSide + holdShapeXStarting + squareSide
								+ mainFieldShiftX, yy * squareSide
								+ nextShapeY2Starting + squareSide
								+ mainFieldShiftY, paint);

			for (int xx = 0; xx < numberOfHoldShapeWidth; xx++)
				for (int yy = 0; yy < numberOfHoldShapeLength; yy++)
					if (next3Blocks[xx][yy] == 1 & nextShape1 == x)
						canvas.drawRect(xx * squareSide + holdShapeXStarting
								+ mainFieldShiftX, yy * squareSide
								+ nextShapeY3Starting + mainFieldShiftY, xx
								* squareSide + holdShapeXStarting + squareSide
								+ mainFieldShiftX, yy * squareSide
								+ nextShapeY3Starting + squareSide
								+ mainFieldShiftY, paint);
		}
		invalidate();
	}

	public static void detectShape() {
		int counter = 0;
		for (int yy = numberOfBlocksLength - 1; yy >= 0; yy--) {
			for (int xx = numberOfBlocksWidth - 1; xx >= 0; xx--) {
				if (blocks[xx][yy] == 1) {
					playLocationX[counter] = xx;
					playLocationY[counter] = yy;
					counter++;
				}
			}
		}
	}

	// Generates new minos.
	// Follows the 7-bag system, which means that there will be ALL the blocks..
	// ...found in each "bag" of 7 pieces.
	// thisShape = -1 means that it is a new game
	// Affects:
	// lastShape == The CURRENT shape on the playing field.
	// thisShape == The NEXT shape on the playing field.
	// nextShape == The NEXT2 shape on the playing field.
	// nextShape1 == The NEXT3 shape on the playing field.
	public static void pickShape() {
		if (thisShape == -1) {
			thisShape = r.nextInt(7);
			timesShapeDrawn[thisShape]++;
			while (nextShape == thisShape | nextShape == -1) {
				nextShape = r.nextInt(7);
			}
			timesShapeDrawn[nextShape]++;
			while (nextShape1 == thisShape | nextShape1 == nextShape
					| nextShape1 == -1) {
				nextShape1 = r.nextInt(7);
			}
			timesShapeDrawn[nextShape1]++;
			pickShapeCounter = 3;
		}
		if (thisShape == 0) {
			for (int xx = 3; xx <= 6; xx++)
				blocks[xx][0] = 1;
			shape = 1;
		} else if (thisShape == 1) {
			for (int xx = 3; xx <= 5; xx++)
				blocks[xx][1] = 1;
			blocks[3][0] = 1;
			shape = 5;
		} else if (thisShape == 2) {
			for (int xx = 3; xx <= 5; xx++)
				blocks[xx][1] = 1;
			blocks[5][0] = 1;
			shape = 9;
		} else if (thisShape == 3) {
			for (int xx = 3; xx <= 5; xx++)
				blocks[xx][1] = 1;
			blocks[4][0] = 1;
			shape = 13;
		} else if (thisShape == 4) {
			blocks[4][0] = 1;
			blocks[5][0] = 1;
			blocks[3][1] = 1;
			blocks[4][1] = 1;
			shape = 15;
		} else if (thisShape == 5) {
			blocks[3][0] = 1;
			blocks[4][0] = 1;
			blocks[4][1] = 1;
			blocks[5][1] = 1;
			shape = 17;
		} else if (thisShape == 6) {
			blocks[4][0] = 1;
			blocks[5][0] = 1;
			blocks[4][1] = 1;
			blocks[5][1] = 1;
			shape = 19;
		}
		lastShape = thisShape;
		thisShape = nextShape;
		nextShape = nextShape1;
		nextShape1 = r.nextInt(7);

		if (timesShapeDrawn[nextShape1] > 0)
			while (timesShapeDrawn[nextShape1] > 0) {
				nextShape1 = r.nextInt(7);
			}
		timesShapeDrawn[nextShape1]++;
		pickShapeCounter++;
		if (pickShapeCounter >= 7) {
			pickShapeCounter = 0;
			for (int xx = 0; xx < timesShapeDrawn.length; xx++)
				timesShapeDrawn[xx] = 0;
		}

		displayBoxShape(nextBlocks, thisShape);
		displayBoxShape(next2Blocks, nextShape);
		displayBoxShape(next3Blocks, nextShape1);
		detectShape();
		holdOnce = false;
	}

	public static void holdShape() {
		if (holdShape == -1) {
			holdShape = lastShape;
			for (int xx = 0; xx < numberOfBlocksWidth; xx++)
				for (int yy = 0; yy < numberOfBlocksLength; yy++)
					if (blocks[xx][yy] == 1)
						blocks[xx][yy] = 0;
			pickShape();
		} else {
			int lastShape1 = holdShape;
			holdShape = lastShape;
			lastShape = lastShape1;
			for (int xx = 0; xx < numberOfBlocksWidth; xx++)
				for (int yy = 0; yy < numberOfBlocksLength; yy++)
					if (blocks[xx][yy] == 1)
						blocks[xx][yy] = 0;
			if (lastShape == 0) {
				for (int xx = 3; xx <= 6; xx++)
					blocks[xx][0] = 1;
				shape = 1;
			} else if (lastShape == 1) {
				for (int xx = 3; xx <= 5; xx++)
					blocks[xx][0] = 1;
				blocks[5][1] = 1;
				shape = 3;
			} else if (lastShape == 2) {
				for (int xx = 3; xx <= 5; xx++)
					blocks[xx][0] = 1;
				blocks[3][1] = 1;
				shape = 7;
			} else if (lastShape == 3) {
				for (int xx = 3; xx <= 5; xx++)
					blocks[xx][0] = 1;
				blocks[4][1] = 1;
				shape = 11;
			} else if (lastShape == 4) {
				blocks[4][0] = 1;
				blocks[5][0] = 1;
				blocks[3][1] = 1;
				blocks[4][1] = 1;
				shape = 15;
			} else if (lastShape == 5) {
				blocks[3][0] = 1;
				blocks[4][0] = 1;
				blocks[4][1] = 1;
				blocks[5][1] = 1;
				shape = 17;
			} else if (lastShape == 6) {
				blocks[3][0] = 1;
				blocks[4][0] = 1;
				blocks[3][1] = 1;
				blocks[4][1] = 1;
				shape = 19;
			}
		}
		holdOnce = true;
		displayBoxShape(holdBlocks, holdShape);
	}

	public static void shapeDown() {
		if (!lose & !pause) {
			boolean move = true;
			detectShape();
			coloring();
			// Detection of whether the block can still fall down.
			if (!slack) {
				for (int xx = 0; xx < playLocationY.length; xx++)
					if (playLocationY[xx] + 1 >= numberOfBlocksLength)
						move = false;
				if (move == true)
					for (int xx = 0; xx < playLocationY.length; xx++)
						if (blocks[playLocationX[xx]][playLocationY[xx] + 1] == 2)
							move = false;
			}
			// Detection Ends

			// Slack Procedure
			if (!move && !hardDrop && !slackOnce) {
				slackOnce = true;
				slack = true;
				move = true;
				time.schedule(new Slack(), slackLength);
			}
			// Slack Procedure Ends.
			if (!move) {
				int currentDrop = 0; // The number of lines cleared
				// T-spin Recognition
				boolean moveUp = true;
				boolean moveLeft = true;
				boolean moveRight = true;
				boolean tSpin = false;

				for (int xx = 0; xx < playLocationY.length; xx++) {
					try {
						if (blocks[playLocationX[xx] - 1][playLocationY[xx]] == 2) {
							moveLeft = false;
						}
					} catch (Exception e) {
						moveLeft = false;
					}
					try {
						if (blocks[playLocationX[xx] + 1][playLocationY[xx]] == 2) {
							moveRight = false;
						}
					} catch (Exception e) {
						moveRight = false;
					}
					try {
						if (blocks[playLocationX[xx]][playLocationY[xx] - 1] == 2) {
							moveUp = false;
						}
					} catch (Exception e) {
						moveUp = false;
					}
				}

				if (!moveLeft & !moveRight & !moveUp) {
					tSpin = true;
				}

				// T-spin recognition end.
				for (int xx = 0; xx < playLocationY.length; xx++)
					blocks[playLocationX[xx]][playLocationY[xx]] = 2;

				for (int yy = numberOfBlocksLength - 1; yy > 0; yy--) {
					boolean line = true;
					do {
						for (int xx = 0; xx < numberOfBlocksWidth; xx++) {
							if (blocks[xx][yy] == 0) {
								line = false;
								break;
							}
						}
						if (line) {
							for (int xx = 0; xx < numberOfBlocksWidth; xx++) {
								blocks[xx][yy] = 0;
							}
							for (int xy = yy; xy > 0; xy--) {
								for (int xx = 0; xx < numberOfBlocksWidth; xx++) {
									blocks[xx][xy] = blocks[xx][xy - 1];
									colors[xx][xy] = colors[xx][xy - 1];
								}
							}
							for (int xx = 0; xx < numberOfBlocksWidth; xx++) {
								blocks[xx][0] = 0;
							}
							currentDrop++;
						}
					} while (line);
				}
				hardDrop = false;
				totalGrav = 0.0;
				gravity = defaultGravity;

				// Scoring System
				int addScore = 0;
				if (currentDrop == 1 & tSpin & !kick) {
					addScore = 800;
					difficult = true;
				} else if (currentDrop == 1 & tSpin & kick) {
					addScore = 200;
					difficult = true;
				} else if (currentDrop == 2 & tSpin) {
					addScore = 1200;
					difficult = true;
				} else if (currentDrop == 3 & tSpin) {
					addScore = 1600;
					difficult = true;
				} else if (currentDrop == 0 & tSpin & kick) {
					addScore = 100;
				} else if (currentDrop == 0 & tSpin) {
					addScore = 400;
				} else if (currentDrop == 1) {
					addScore = 100;
				} else if (currentDrop == 2) {
					addScore = 300;
				} else if (currentDrop == 3) {
					addScore = 500;
				} else if (currentDrop == 4) {
					addScore = 800;
					difficult = true;
				}

				if (lastDifficult & difficult) {
					addScore = (int) (addScore * 1.5);
				}
				addScore = addScore + 50 * combo;
				if (currentDrop != 0) {
					combo = combo + 1;
				} else {
					combo = 0;
				}

				score = score + addScore;
				// Scoring System end.

				pickShape();
			} else {
				for (int xx = 0; xx < playLocationX.length; xx++) {
					if (!slack) {
						blocks[playLocationX[xx]][playLocationY[xx]] = 0;
						blocks[playLocationX[xx]][playLocationY[xx] + 1] = 1;
						slackOnce = false;
					}
				}
			}
			if (move && !slack) {
				if (hardDrop) {
					score = score + 2;
				} else if (softDrop) {
					score = score + 1;
				}
			}
			lose = false;
			for (int xx = 0; xx < numberOfBlocksWidth; xx++) {
				if (blocks[xx][2] == 2) {
					lose = true;
					// TODO set lose message
					break;
				}
			}
		}
	}

	public static void ghostShape() {
		boolean move = true;
		detectShape();
		int[] tempPlayLocationX = new int[4];
		int[] tempPlayLocationY = new int[4];
		for (int xx = 0; xx < numberOfBlocksWidth; xx++)
			for (int yy = 0; yy < numberOfBlocksLength; yy++)
				if (blocks[xx][yy] == 3)
					blocks[xx][yy] = 0;
		for (int xx = 0; xx < playLocationY.length; xx++) {
			tempPlayLocationX[xx] = playLocationX[xx];
			tempPlayLocationY[xx] = playLocationY[xx];
		}
		do {
			for (int xx = 0; xx < playLocationY.length; xx++)
				if (tempPlayLocationY[xx] + 1 >= numberOfBlocksLength)
					move = false;
			if (move)
				for (int xx = 0; xx < playLocationY.length; xx++)
					if (blocks[tempPlayLocationX[xx]][tempPlayLocationY[xx] + 1] == 2)
						move = false;
			if (!move) {
				for (int xx = 0; xx < playLocationY.length; xx++)
					if (blocks[tempPlayLocationX[xx]][tempPlayLocationY[xx]] != 1
							& blocks[tempPlayLocationX[xx]][tempPlayLocationY[xx]] != 2)
						blocks[tempPlayLocationX[xx]][tempPlayLocationY[xx]] = 3;
			} else
				for (int xx = 0; xx < playLocationY.length; xx++)
					tempPlayLocationY[xx] = tempPlayLocationY[xx] + 1;
		} while (move);
	}

	public static void moveLeft() {
		boolean move = true;
		detectShape();
		for (int xx = 0; xx < playLocationY.length; xx++)
			if (playLocationX[xx] - 1 < 0)
				move = false;
		if (move)
			for (int xx = 0; xx < playLocationY.length; xx++)
				if (blocks[playLocationX[xx] - 1][playLocationY[xx]] == 2)
					move = false;
		if (move) {
			for (int xx = 0; xx < playLocationY.length; xx++)
				blocks[playLocationX[xx]][playLocationY[xx]] = 0;
			for (int xx = 0; xx < playLocationY.length; xx++)
				blocks[playLocationX[xx] - 1][playLocationY[xx]] = 1;
		}
	}

	public static void moveRight() {
		boolean move = true;
		detectShape();
		for (int xx = 0; xx < playLocationY.length; xx++)
			if (playLocationX[xx] + 1 >= numberOfBlocksWidth)
				move = false;
		if (move)
			for (int xx = 0; xx < playLocationY.length; xx++)
				if (blocks[playLocationX[xx] + 1][playLocationY[xx]] == 2)
					move = false;
		if (move) {
			for (int xx = 0; xx < playLocationY.length; xx++)
				blocks[playLocationX[xx]][playLocationY[xx]] = 0;
			for (int xx = 0; xx < playLocationY.length; xx++)
				blocks[playLocationX[xx] + 1][playLocationY[xx]] = 1;
		}
	}

	public static void shapeTurn() {
		turnSuccess = false;
		detectShape();
		if (shape == 1) {
			turnShape(-2, -1, 0, 1, 2, 1, 0, -1);
			if (turnSuccess)
				shape = 2;
		} else if (shape == 2) {
			turnShape(-2, -1, 0, 1, -2, -1, 0, 1);
			if (turnSuccess)
				shape = 1;
		} else if (shape == 3) {
			turnShape(-2, -1, 0, 1, 0, 1, 0, -1);
			if (turnSuccess)
				shape = 4;
		} else if (shape == 4) {
			turnShape(-1, 0, 0, 1, -1, -2, 0, 1);
			if (turnSuccess)
				shape = 5;
		} else if (shape == 5) {
			turnShape(-1, 0, 1, 2, 1, 0, -1, 0);
			if (turnSuccess)
				shape = 6;
		} else if (shape == 6) {
			turnShape(-1, 0, 0, 1, -1, 0, 2, 1);
			if (turnSuccess)
				shape = 3;
		} else if (shape == 7) {
			turnShape(0, -1, 0, 1, -2, 1, 0, -1);
			if (turnSuccess)
				shape = 8;
		} else if (shape == 8) {
			turnShape(-1, 0, 1, 2, -1, 0, 1, 0);
			if (turnSuccess)
				shape = 9;
		} else if (shape == 9) {
			turnShape(-1, 0, 1, 0, 1, 0, -1, 2);
			if (turnSuccess)
				shape = 10;
		} else if (shape == 10) {
			turnShape(-2, -1, 0, 1, 0, -1, 0, 1);
			if (turnSuccess)
				shape = 7;
		} else if (shape == 11) {
			turnShape(0, -1, 0, 0, 0, -1, 0, 0);
			if (turnSuccess)
				shape = 12;
		} else if (shape == 12) {
			turnShape(1, 0, 0, 0, -1, 0, 0, 0);
			if (turnSuccess)
				shape = 13;
		} else if (shape == 13) {
			turnShape(0, 0, 1, 0, 0, 0, 1, 0);
			if (turnSuccess)
				shape = 14;
		} else if (shape == 14) {
			turnShape(0, 0, 0, -1, 0, 0, 0, 1);
			if (turnSuccess)
				shape = 11;
		} else if (shape == 15) {
			turnShape(0, 0, -2, 0, 0, -1, -1, 0);
			if (turnSuccess)
				shape = 16;
		} else if (shape == 16) {
			turnShape(0, 0, 0, 2, 0, 0, 1, 1);
			if (turnSuccess)
				shape = 15;
		} else if (shape == 17) {
			turnShape(-1, -1, 0, 0, -2, 0, 0, 0);
			if (turnSuccess)
				shape = 18;
		} else if (shape == 18) {
			turnShape(1, 0, 0, 1, 0, 0, 0, 2);
			if (turnSuccess)
				shape = 17;
		}
	}

	public static void shapeTurnCC() {
		turnSuccess = false;
		detectShape();
		if (shape == 1) {
			turnShape(-2, -1, 0, 1, 2, 1, 0, -1);
			if (turnSuccess)
				shape = 2;
		} else if (shape == 2) {
			turnShape(-2, -1, 0, 1, -2, -1, 0, 1);
			if (turnSuccess)
				shape = 1;
		} else if (shape == 3) {
			turnShape(0, -1, 0, 1, -2, -1, 0, 1);
			if (turnSuccess)
				shape = 6;
		} else if (shape == 4) {
			turnShape(1, 2, 0, -1, -1, 0, 0, 1);
			if (turnSuccess)
				shape = 3;
		} else if (shape == 5) {
			turnShape(-1, 0, 1, 0, -1, 0, 1, 2);
			if (turnSuccess)
				shape = 4;
		} else if (shape == 6) {
			turnShape(1, 0, -2, -1, -1, 0, 1, 0);
			if (turnSuccess)
				shape = 5;
		} else if (shape == 7) {
			turnShape(2, -1, 0, 1, 0, -1, 0, 1);
			if (turnSuccess)
				shape = 10;
		} else if (shape == 8) {
			turnShape(1, 0, -1, 0, -1, 0, 1, 2);
			if (turnSuccess)
				shape = 7;
		} else if (shape == 9) {
			turnShape(-1, 0, 1, -2, -1, 0, 1, 0);
			if (turnSuccess)
				shape = 8;
		} else if (shape == 10) {
			turnShape(0, 1, 0, -1, -2, -1, 0, 1);
			if (turnSuccess)
				shape = 9;
		} else if (shape == 11) {
			turnShape(0, 0, 0, 1, 0, 0, 0, -1);
			if (turnSuccess)
				shape = 14;
		} else if (shape == 12) {
			turnShape(0, 0, 0, 1, 0, 0, 0, 1);
			if (turnSuccess)
				shape = 11;
		} else if (shape == 13) {
			turnShape(-1, 0, 0, 0, 1, 0, 0, 0);
			if (turnSuccess)
				shape = 12;
		} else if (shape == 14) {
			turnShape(-1, 0, 0, 0, -1, 0, 0, 0);
			if (turnSuccess)
				shape = 13;
		} else if (shape == 15) {
			turnShape(0, 0, -2, 0, 0, -1, -1, 0);
			if (turnSuccess)
				shape = 16;
		} else if (shape == 16) {
			turnShape(0, 0, 0, 2, 0, 0, 1, 1);
			if (turnSuccess)
				shape = 15;
		} else if (shape == 17) {
			turnShape(-1, -1, 0, 0, -2, 0, 0, 0);
			if (turnSuccess)
				shape = 18;
		} else if (shape == 18) {
			turnShape(1, 0, 0, 1, 0, 0, 0, 2);
			if (turnSuccess)
				shape = 17;
		}
	}

	public static void turnShape(int x1, int x2, int x3, int x4, int y1,
			int y2, int y3, int y4) {
		kick = false;
		int[] x = new int[4];
		int[] y = new int[4];
		x[0] = x1;
		x[1] = x2;
		x[2] = x3;
		x[3] = x4;
		y[0] = y1;
		y[1] = y2;
		y[2] = y3;
		y[3] = y4;

		for (int yy = 0; yy <= 2; yy++) {

			if (yy == 1)
				yy = -1;
			else if (yy != 0)
				yy = yy - 1;
			for (int xy = 0; xy <= 3; xy++) {

				if (xy == 1)
					xy = -1;
				else if (xy != 0)
					xy = xy - 1;

				boolean ok = true;

				for (int xx = 0; xx < playLocationX.length; xx++) {
					if (playLocationX[xx] + x[xx] + xy > numberOfBlocksWidth - 1
							| playLocationX[xx] + x[xx] + xy < 0) {
						ok = false;
					}
				}
				for (int xx = 0; xx < playLocationY.length; xx++) {
					if (playLocationY[xx] + y[xx] - yy >= 22
							| playLocationY[xx] + y[xx] - yy < 0) {
						ok = false;
					}
				}
				try {
					if (ok) {
						if (blocks[playLocationX[0] + x[0] + xy][playLocationY[0]
								+ y[0] - yy] != 2
								& blocks[playLocationX[1] + x[1] + xy][playLocationY[1]
										+ y[1] - yy] != 2
								& blocks[playLocationX[2] + x[2] + xy][playLocationY[2]
										+ y[2] - yy] != 2
								& blocks[playLocationX[3] + x[3] + xy][playLocationY[3]
										+ y[3] - yy] != 2) {
							for (int xx = 0; xx < playLocationY.length; xx++)
								blocks[playLocationX[xx]][playLocationY[xx]] = 0;
							for (int xx = 0; xx < playLocationY.length; xx++)
								blocks[playLocationX[xx] + x[xx] + xy][playLocationY[xx]
										+ y[xx] - yy] = 1;
							turnSuccess = true;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (xy == -1)
					xy = 1;
				else if (xy != 0)
					xy = xy + 1;
				if (turnSuccess) {
					if (xy != 0) {
						kick = true;
					}
					break;
				}
			}
			if (yy == -1)
				yy = 1;
			else if (yy != 0)
				yy = yy + 1;
			if (turnSuccess) {
				if (yy != 0) {
					kick = true;
				}
				break;
			}
		}
	}

	public static void coloring() {
		detectShape();
		for (int xx = 0; xx < JetrisMainView.playLocationX.length; xx++)
			JetrisMainView.colors[JetrisMainView.playLocationX[xx]][JetrisMainView.playLocationY[xx]] = JetrisMainView.lastShape;
	}

	public static void displayBoxShape(int[][] x, int y) {
		for (int xx = 0; xx < numberOfHoldShapeWidth; xx++)
			for (int yy = 0; yy < numberOfHoldShapeLength; yy++)
				x[xx][yy] = 0;

		if (y == 0) {
			for (int xx = 0; xx <= 3; xx++)
				x[xx][0] = 1;
		} else if (y == 1) {
			for (int xx = 0; xx <= 2; xx++)
				x[xx][1] = 1;
			x[0][0] = 1;
		} else if (y == 2) {
			for (int xx = 0; xx <= 2; xx++)
				x[xx][1] = 1;
			x[2][0] = 1;
		} else if (y == 3) {
			for (int xx = 0; xx <= 2; xx++)
				x[xx][1] = 1;
			x[1][0] = 1;
		} else if (y == 4) {
			x[1][0] = 1;
			x[2][0] = 1;
			x[0][1] = 1;
			x[1][1] = 1;
		} else if (y == 5) {
			x[0][0] = 1;
			x[1][0] = 1;
			x[1][1] = 1;
			x[2][1] = 1;
		} else if (y == 6) {
			x[0][0] = 1;
			x[1][0] = 1;
			x[0][1] = 1;
			x[1][1] = 1;
		}
	}

	public static void newGame() {
		for (int xx = 0; xx < numberOfBlocksWidth; xx++) {
			for (int yy = 0; yy < numberOfBlocksLength; yy++) {
				blocks[xx][yy] = 0;
				colors[xx][yy] = 0;
				if (xx < numberOfHoldShapeWidth & yy < numberOfHoldShapeLength) {
					holdBlocks[xx][yy] = 0;
					nextBlocks[xx][yy] = 0;
					next2Blocks[xx][yy] = 0;
					next3Blocks[xx][yy] = 0;
				}
			}
		}
		pause = false;
		blocks = new int[numberOfBlocksWidth][numberOfBlocksLength];
		colors = new int[numberOfBlocksWidth][numberOfBlocksLength];
		holdBlocks = new int[numberOfHoldShapeWidth][numberOfHoldShapeLength];
		nextBlocks = new int[numberOfHoldShapeWidth][numberOfHoldShapeLength];
		next2Blocks = new int[numberOfHoldShapeWidth][numberOfHoldShapeLength];
		next3Blocks = new int[numberOfHoldShapeWidth][numberOfHoldShapeLength];
		playLocationX = new int[4];
		playLocationY = new int[4];
		timesShapeDrawn = new int[7];
		pickShapeCounter = 0;
		thisShape = -1;
		nextShape = -1;
		nextShape1 = -1;
		holdShape = -1;
		shape = -1;
		lastShape = -1;
		score = 0;
		lose = false;
		pickShape();
		time.cancel();
		time = new Timer();
	}

}