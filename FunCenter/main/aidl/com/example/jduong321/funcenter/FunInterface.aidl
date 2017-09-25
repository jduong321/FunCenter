// FunInterface.aidl
package com.example.jduong321.funcenter;

// Declare any non-default types here with import statements

interface FunInterface {
    void stop();
    void pause();
    void play(int n);
    void resume();
    String display(int n);
    boolean getStatus();
    int getPosition();
}
