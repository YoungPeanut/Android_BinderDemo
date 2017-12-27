// ISecondary.aidl
package cs.shawn.binderdemo.aidl;

// Declare any non-default types here with import statements

interface ISecondary {


    /**
     * Request the PID of this service, to do evil things with it.
     */
    int getPid();

    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
}
