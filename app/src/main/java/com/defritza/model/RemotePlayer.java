package com.defritza.model;

import com.defritza.control.Chess;
import com.defritza.control.Playable;
import com.defritza.util.Move;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class encapsulates network functionality required to communicate with a remote host, and at a high level, represents
 * the player on the remote host.
 * @author Chris Fretz, Joseph
 */
public class RemotePlayer extends Player implements Playable {
  private static final long serialVersionUID = 1;
  private BlockingQueue<Move> incoming, outgoing;
  private transient Thread worker;
  private String address;
  private short port;
  private boolean alive;

  /**
   * Constructs a RemotePlayer for a hosting device.
   * @param port Port that the RemotePlayer should expect to receive communications on.
   */
  public RemotePlayer(short port) {
    this("", port);
  }

  /**
   * Constructs a RemotePlayer for a client device.
   * @param address IP address that the RemotePlayer object should connect to.
   * @param port    Port number that the RemotePlayer should connect to.
   */
  public RemotePlayer(String address, short port) {
    super(address.equals("") ? "Black" : "White");
    this.address = address;
    this.port = port;
    incoming = new LinkedBlockingQueue<Move>();
    outgoing = new LinkedBlockingQueue<Move>();
  }

  @Override
  public void start() {
    worker = new Thread(new MiddleMan(incoming, outgoing, address, port, address.equals("")));
    worker.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(Thread t, Throwable e) {
        alive = false;
      }
    });
    worker.start();
    alive = true;
  }

  @Override
  public Move getMove(Chess game, Board board) {
    return incoming.poll();
  }

  @Override
  public boolean updatePlayer(Move move) {
    if (!alive) return false;
    outgoing.offer(move);
    return true;
  }

  @Override
  public void finish() {
    worker.interrupt();
    try {
      worker.join();
    } catch (InterruptedException e) {
      // Oh well.
    }
  }

  /**
   * Method checks if the RemotePlayer object is still in a valid state with respect to network communication.
   * If this method returns false, it indicates that a fatal asynchronous network error of some kind has occurred.
   * Game state is invalid if this occurs. Currently no recovery strategy is implemented, so the game must be abandoned in this case.
   * @return Whether or not the RemotePlayer object is still alive and healthy.
   */
  public boolean alive() {
    return alive;
  }

  private class MiddleMan implements Runnable {
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private BlockingQueue<Move> incoming, outgoing;
    private String address;
    private short port;
    private boolean server, shouldRead;

    protected MiddleMan(BlockingQueue<Move> incoming, BlockingQueue<Move> outgoing, String address, short port, boolean server) {
      this.incoming = incoming;
      this.outgoing = outgoing;
      this.address = address;
      this.port = port;
      this.server = server;
      shouldRead = !server;
    }

    @Override
    public void run() {
      if (server) {
        try {
          socket = (new ServerSocket(port)).accept();
          output = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
          throw new IllegalStateException("Listening socket threw an exception during setup");
        }
      } else {
        try {
          socket = new Socket(address, port);
          input = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
          throw new IllegalStateException("Client socket threw an exception during setup");
        }
      }
      dataLoop();
      try {
        output.close();
        input.close();
        socket.close();
      } catch (Exception e) {
        // Oh well.
      }
    }

    private void dataLoop() {
      while (!Thread.interrupted()) {
        if (shouldRead) {
          Move move;
          try {
            move = (Move) input.readObject();
          } catch (InterruptedIOException e) {
            continue;
          } catch (IOException e) {
            throw new IllegalStateException("Object input stream threw an IOException while reading an object");
          } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Object input stream threw a ClassNotFoundException while reading an object");
          }
          incoming.offer(move);
          try {
            if (output == null)
              output = new ObjectOutputStream(socket.getOutputStream());
          } catch (IOException e) {
            throw new IllegalStateException("Object output stream threw an exception during instantiation in the client");
          }
        } else {
          Move move;
          try {
            move = outgoing.take();
          } catch (InterruptedException e) {
            continue;
          }
          try {
            output.writeObject(move);
          } catch (InterruptedIOException e) {
            continue;
          } catch (IOException e) {
            throw new IllegalStateException("Object output stream threw an IOException while writing an object");
          }
          try {
            if (input == null) input = new ObjectInputStream(socket.getInputStream());
          } catch (IOException e) {
            throw new IllegalStateException("Object input stream threw an exception during instantiation in the server");
          }
        }
        shouldRead = !shouldRead;
      }
    }
  }
}