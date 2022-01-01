/*
 * Copyright (c) Octavia Togami <https://octyl.net>
 * Copyright (c) contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.octyl.graalfudge.language.wutlang;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleLanguage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class WutlangContext implements Closeable {
    private record InputOutput(
        InputStream in,
        OutputStream out
    ) {
    }

    private final TruffleLanguage.Env env;
    private final WutlangLanguage language;
    private final InputOutput defaultIO;
    private InputOutput networkIO;
    private InputOutput fileIO;
    private InputOutput currentIO;
    private Thread acceptingThread;
    private final Lock socketLock = new ReentrantLock();
    private final Condition socketDeath = socketLock.newCondition();
    private Socket socket;

    public WutlangContext(
        TruffleLanguage.Env env,
        WutlangLanguage language,
        InputStream input,
        OutputStream output
    ) {
        this.env = env;
        this.language = language;
        this.defaultIO = new InputOutput(input, output);
        this.currentIO = defaultIO;
    }

    public WutlangContext(WutlangLanguage language, TruffleLanguage.Env env) {
        this(
            env,
            language,
            new BufferedInputStream(env.in()),
            new BufferedOutputStream(env.out())
        );
    }

    public TruffleLanguage.Env env() {
        return env;
    }

    public WutlangLanguage language() {
        return language;
    }

    public InputStream input() {
        return currentIO.in;
    }

    public OutputStream output() {
        return currentIO.out;
    }

    private void useInput(InputOutput source) {
        currentIO = new InputOutput(source.in, currentIO.out);
    }

    private void useOutput(InputOutput source) {
        currentIO = new InputOutput(currentIO.in, source.out);
    }

    public void useNetworkInput() {
        useInput(networkIO);
    }

    public void useNetworkOutput() {
        useOutput(networkIO);
    }

    public void useFileInput() {
        useInput(fileIO);
    }

    public void useFileOutput() {
        useOutput(fileIO);
    }

    public void useDefaultInput() {
        useInput(defaultIO);
    }

    public void useDefaultOutput() {
        useOutput(defaultIO);
    }

    @CompilerDirectives.TruffleBoundary
    public void openSocket(int port) {
        closeSocket();
        closeServer();
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        acceptingThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                this.socketLock.lock();
                try {
                    try {
                        this.socket = serverSocket.accept();
                        this.networkIO = new InputOutput(
                            socket.getInputStream(),
                            socket.getOutputStream()
                        );
                    } catch (IOException e) {
                        System.err.println("Failed to accept connection:");
                        e.printStackTrace();
                    }
                    // Await the socket being lost
                    this.socketDeath.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    this.socketLock.unlock();
                }
            }
        }, "wutlang-socket-listener");
        acceptingThread.setDaemon(true);
    }

    @CompilerDirectives.TruffleBoundary
    public void closeSocket() {
        socketLock.lock();
        try {
            flush();
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            socket = null;
            networkIO = null;
            socketDeath.signalAll();
            socketLock.unlock();
        }
    }

    @CompilerDirectives.TruffleBoundary
    public void closeServer() {
        if (acceptingThread != null) {
            acceptingThread.interrupt();
            try {
                acceptingThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }

    public void flush() throws IOException {
        defaultIO.out.flush();
        if (networkIO != null) {
             networkIO.out.flush();
        }
        if (fileIO != null) {
            fileIO.out.flush();
        }
    }

    @Override
    public void close() throws IOException {
        closeServer();
        closeSocket();
    }

    @Override
    public String toString() {
        return "WutlangContext["
            + "env=" + env + ", "
            + "language=" + language + ", "
            + "useNetworkIo=" + useNetworkIo
            + ']';
    }
}
