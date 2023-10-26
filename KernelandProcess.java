import java.util.Arrays;

public class KernelandProcess {
    private static int nextpid = 0;
    private final int pid;
    private boolean started;
    private Thread thread;
    private Priority priority;
    private final Object priorityLock = new Object();
    private int demoteFuse = 5;
    public static final int MAXDEVICES = 10;
    private final int[] deviceIds = new int[MAXDEVICES];
    
    KernelandProcess(UserlandProcess up){
        Arrays.fill(deviceIds, -1);
        pid = nextpid++;
        thread = new Thread(up, String.format("[Kerneland] Process, PID%d", pid));
        priority = Priority.INTERACTIVE;
    }
    KernelandProcess(UserlandProcess up, Priority p){
        Arrays.fill(deviceIds, -1);
        pid = nextpid++;
        thread = new Thread(up, String.format("[Kerneland] Process, PID%d", pid));
        priority = p;
    }
    
    public void stop(){
        
        
        if(started) thread.suspend();
            else throw new UnsupportedOperationException(String.format("Process PID%d could not be stopped, was not running", pid));
    }
    
    public boolean isDone(){
        if(started && !thread.isAlive()) return true;
            else return false;
    }
    
    public void run(){
        if(started) thread.resume();
            else {
                started = true;
                thread.start();
            }
   }
   
   public boolean burnFuse(){
        demoteFuse--;
        if(Debug.flag)
           System.out.printf("$> Process PID%d burned %d/5 fuses%n", pid, 5 - demoteFuse);
        return checkDemotion();
   }
   private boolean checkDemotion(){
        synchronized (priorityLock) {
            if (demoteFuse == 0) {
                demoteFuse = 5;
                if (priority == Priority.REALTIME) priority = Priority.INTERACTIVE;
                else if (priority == Priority.INTERACTIVE) priority = Priority.BACKGROUND;
                // Backgrounds can't get demoted further
                if (Debug.flag)
                    System.out.printf("$> Process PID%d demoted to %s%n", pid, priority);
                return true;
            }
            return false;
        }
   }
   
   public int getDeviceId(int i){ return deviceIds[i]; }
    public void setDeviceId(int i, int id){ deviceIds[i] = id; }
   public int getPid(){ return pid; }
    
    
    public Priority getPriority(){ synchronized (priorityLock) {return priority;} }
    public String toString(){
          return String.format("PID%d", pid);
    }
   
}