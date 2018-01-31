package gitlet;

import java.io.*;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Mudabbir Khan
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        switch (args[0].toLowerCase()) {
        case "init":
            if (args.length == 1) {
                File g = new File(".gitlet");
                if (g.exists()) {
                    System.out.println("A Gitlet version-control system already " +
                            "exists in the current directory.");
                    System.exit(0);
                }
                Registry gitlet = new Registry();
                gitlet.init();
                File allData = new File(".gitlet/AllData");
                Utils.writeObject(allData, gitlet);
            } else {
                System.out.println("Incorrect operands.");
            }
            System.exit(0);
            break;
        case "log":
            if (args.length == 1) {
                File g = new File(".gitlet");
                if (!g.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                File allData = new File(".gitlet/AllData");
                Registry gitlet = Utils.readObject(allData, Registry.class);
                gitlet.log();
                Utils.writeObject(allData, gitlet);
            } else {
                System.out.println("Incorrect operands.");
            }
            System.exit(0);
            break;
        case "global-log":
            if (args.length == 1) {
                File g = new File(".gitlet");
                if (!g.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                File allData = new File(".gitlet/AllData");
                Registry gitlet = Utils.readObject(allData, Registry.class);
                gitlet.gLog();
                Utils.writeObject(allData, gitlet);
            } else {
                System.out.println("Incorrect operands.");
            }
            System.exit(0);
            break;
        case "status":
            if (args.length == 1) {
                File g = new File(".gitlet");
                if (!g.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                File allData = new File(".gitlet/AllData");
                Registry gitlet = Utils.readObject(allData, Registry.class);
                gitlet.status();
                Utils.writeObject(allData, gitlet);
            } else {
                System.out.println("Incorrect operands.");
            }
            System.exit(0);
            break;
        case "add":
            if (args.length == 2) {
                File g = new File(".gitlet");
                if (!g.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                File allData = new File(".gitlet/AllData");
                Registry gitlet = Utils.readObject(allData, Registry.class);
                gitlet.add(args[1]);
                Utils.writeObject(allData, gitlet);
            } else {
                System.out.println("Incorrect operands.");
            }
            System.exit(0);
            break;
        case "commit":
            if (args.length <= 2) {
                File g = new File(".gitlet");
                if (!g.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                try {
                    File allData = new File(".gitlet/AllData");
                    Registry gitlet = Utils.readObject(allData, Registry.class);
                    gitlet.commit(args[1]);
                    Utils.writeObject(allData, gitlet);
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Please enter a commit message.");
                }
            } else {
                System.out.println("Incorrect operands.");
            }
            System.exit(0);
            break;
        case "rm":
            if (args.length == 2) {
                File g = new File(".gitlet");
                if (!g.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                File allData = new File(".gitlet/AllData");
                Registry gitlet = Utils.readObject(allData, Registry.class);
                gitlet.remove(args[1]);
                Utils.writeObject(allData, gitlet);
            } else {
                System.out.println("Incorrect operands.");
            }
            System.exit(0);
            break;
        case "find":
            if (args.length == 2) {
                File g = new File(".gitlet");
                if (!g.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                File allData = new File(".gitlet/AllData");
                Registry gitlet = Utils.readObject(allData, Registry.class);
                gitlet.find(args[1]);
                Utils.writeObject(allData, gitlet);
            } else {
                System.out.println("Incorrect operands.");
            }
            System.exit(0);
            break;
        case "branch":
            if (args.length == 2) {
                File g = new File(".gitlet");
                if (!g.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                File allData = new File(".gitlet/AllData");
                Registry gitlet = Utils.readObject(allData, Registry.class);
                gitlet.branch(args[1]);
                Utils.writeObject(allData, gitlet);
            } else {
                System.out.println("Incorrect operands.");
            }
            System.exit(0);
            break;
        case "rm-branch":
            if (args.length == 2) {
                File g = new File(".gitlet");
                if (!g.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                File allData = new File(".gitlet/AllData");
                Registry gitlet = Utils.readObject(allData, Registry.class);
                gitlet.removeBranch(args[1]);
                Utils.writeObject(allData, gitlet);
            } else {
                System.out.println("Incorrect operands.");
            }
            System.exit(0);
            break;
        case "reset":
            if (args.length == 2) {
                File g = new File(".gitlet");
                if (!g.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                File allData = new File(".gitlet/AllData");
                Registry gitlet = Utils.readObject(allData, Registry.class);
                gitlet.reset(args[1]);
                Utils.writeObject(allData, gitlet);
            } else {
                System.out.println("Incorrect operands.");
            }
            System.exit(0);
            break;
        case "merge":
            if (args.length == 2) {
                File g = new File(".gitlet");
                if (!g.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                File allData = new File(".gitlet/AllData");
                Registry gitlet = Utils.readObject(allData, Registry.class);
                gitlet.merge(args[1]);
                Utils.writeObject(allData, gitlet);
            } else {
                System.out.println("Incorrect operands.");
            }
            System.exit(0);
            break;
        case "checkout":
            if (args.length == 3) {
                if (args[1].equals("--")) {
                    File g = new File(".gitlet");
                    if (!g.exists()) {
                        System.out.println("Not in an initialized Gitlet directory.");
                        System.exit(0);
                    }
                    File allData = new File(".gitlet/AllData");
                    Registry gitlet = Utils.readObject(allData, Registry.class);
                    gitlet.fileCheckout(args[2]);
                    Utils.writeObject(allData, gitlet);
                } else {
                    System.out.println("Incorrect operands.");
                }
            } else if (args.length == 4) {
                if (args[2].equals("--")) {
                    File g = new File(".gitlet");
                    if (!g.exists()) {
                        System.out.println("Not in an initialized Gitlet directory.");
                        System.exit(0);
                    }
                    File allData = new File(".gitlet/AllData");
                    Registry gitlet = Utils.readObject(allData, Registry.class);
                    gitlet.checkout(args[1], args[3]);
                    Utils.writeObject(allData, gitlet);
                } else {
                    System.out.println("Incorrect operands.");
                }
            } else if (args.length == 2) {
                File g = new File(".gitlet");
                if (!g.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                File allData = new File(".gitlet/AllData");
                Registry gitlet = Utils.readObject(allData, Registry.class);
                gitlet.branchCheckout(args[1]);
                Utils.writeObject(allData, gitlet);
            } else {
                System.out.println("Incorrect operands.");
            }
            System.exit(0);
            break;
        default:
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
    }

}
