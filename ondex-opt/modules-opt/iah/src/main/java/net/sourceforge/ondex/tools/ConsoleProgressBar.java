package net.sourceforge.ondex.tools;

public class ConsoleProgressBar {
	
	private int progress = 0, maxProgress, displayed = 0;
	private final int l = 72;
	
	public ConsoleProgressBar(int maxProgress) {
		this.maxProgress = maxProgress;
		System.out.println("0%              25%              50%              75%              100%");
	}
	
	public void inc(int p) {
		progress += p;
		int toDisplay = (int)(((double)progress)/((double)maxProgress)*((double)l));
		int toAdd = toDisplay - displayed;
		for (int i = 0 ; i < toAdd; i++) {
			System.out.print("█");
		}
		displayed = toDisplay;
	}
	
	public void complete() {
		inc(maxProgress - progress);
		System.out.println("\n");
	}
	
	public static void main(String[] args) {
		ConsoleProgressBar pb = new ConsoleProgressBar(5000);
		for (int i = 0; i < 5000; i+=28) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
			pb.inc(28);
		}
		pb.complete();
	}
}
