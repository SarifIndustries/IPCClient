// IPalisadeAIDL.aidl
package temple.ground.aidlserver;

interface IPalisadeAIDL {
    /** Request the process ID of this service */
    int getPid();

    /** Count of received connection requests from clients */
    int getConnectionCount();

    /** Set displayed value of screen */
    void setDisplayedValue(in String packageName, int pid, String text);
}