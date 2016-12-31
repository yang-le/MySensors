package me.yangle.myphone;

class OclHelper {
    static {
        System.loadLibrary("oclHelper-lib");
    }

    public static native void Hello();
}
