package gitlet;

/** The Registry class that acts as the version control repository, handling
*   all command line arguments and implementing all the required functions.
*   Also controls the reading and writing of data.
*   @author Mudabbir Khan 
*/

import java.io.*;
import java.util.*;

public class Registry implements Serializable {

    public Registry() {
    }

    public void init() {
        File gitlet = new File(".gitlet");
        gitlet.mkdir();
        File staged = new File(".gitlet" + File.separator + "staged");
        staged.mkdir();
        File removed = new File(".gitlet" + File.separator + "removed");
        removed.mkdir();
        File commits = new File(".gitlet" + File.separator + "commits");
        commits.mkdir();
        File branches = new File(".gitlet" + File.separator + "branches");
        branches.mkdir();
        Commit init = new Commit(null, "initial commit", null);
        init.setCommitHash();
        head = init.getCommitHash();
        _commits.put(head, init);
        File initCommit = new File(".gitlet" + File.separator
                + "commits" + File.separator + head);
        Utils.writeObject(initCommit, init);
        currentBranch = "master";
        _branches.put(head, currentBranch);
        File firstBranch = new File(".gitlet" + File.separator
                + "branches" + File.separator + currentBranch);
        Utils.writeContents(firstBranch, head);
    }

    public void add(String fileName) {
        File addFile = new File(fileName);
        if (!addFile.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        File removeFile = new File(".gitlet" + File.separator
                + "removed" + File.separator + fileName);
        if (removeFile.exists()) {
            removeFile.delete();
        }
        Commit parentCommit = _commits.get(head);
        HashMap<String, String> parentFiles = parentCommit.getBlobs();
        String fileHash = "blob " + fileName + " " + Utils.readContentsAsString(addFile);
        byte[] readFile = Utils.readContents(addFile);
        File stageFile = new File(".gitlet" + File.separator
                + "staged" + File.separator + fileName);
        if (parentFiles != null && parentFiles.get(fileName) != null
                && parentFiles.get(fileName).equals(Utils.sha1(fileHash))) {
            if (stageFile.exists()) {
                stageFile.delete();
            }
        } else {
            Utils.writeContents(stageFile, readFile);
        }
    }

    public void commit(String msg) {
        File stageFile = new File(".gitlet" + File.separator + "staged");
        File removeFile = new File(".gitlet" + File.separator + "removed");
        if (Utils.plainFilenamesIn(stageFile) != null
                && Utils.plainFilenamesIn(stageFile).size() == 0
                && Utils.plainFilenamesIn(removeFile) != null
                && Utils.plainFilenamesIn(removeFile).size() == 0) {
            System.out.println("No changes added to the commit.");
            return;

        } else if (msg.equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        }
        Commit newCommit = new Commit(_commits.get(head), msg, null);
        newCommit.addParentBlobs();
        newCommit.stageFiles();
        newCommit.removeFiles();
        newCommit.setCommitHash();
        head = newCommit.getCommitHash();
        _commits.put(head, newCommit);
        _branches.put(head, currentBranch);
        File nextCommit = new File(".gitlet" + File.separator
                + "commits" + File.separator + head);
        Utils.writeObject(nextCommit, newCommit);
        File newBranch = new File(".gitlet" + File.separator
                + "branches" + File.separator + currentBranch);
        Utils.writeContents(newBranch, head);
    }

    public void remove(String fileName) {
        File stageFile = new File(".gitlet" + File.separator
                + "staged" + File.separator + fileName);
        File workingFile = new File(fileName);
        Commit c = _commits.get(head);
        List<String> trackFiles = c.getBlobNames();
        if (trackFiles != null) {
            if (!stageFile.exists() && !trackFiles.contains(fileName)) {
                System.out.println("No reason to remove the file.");
                System.exit(0);
            }
        }
        if (stageFile.exists()) {
            stageFile.delete();
        }
        File removeFile = new File(".gitlet" + File.separator
                + "removed" + File.separator + fileName);
        if (trackFiles != null) {
            if (trackFiles.contains(fileName)) {
                Utils.writeContents(removeFile, c.getContent(fileName));
                workingFile.delete();
            }
        }
    }

    public void log() {
        Commit c = _commits.get(head);
        while (c != null) {
            System.out.println("===");
            System.out.println("commit " + c.getCommitHash());
            if (c.getMerged() != null) {
                System.out.println("Merge: " + c.getParent().getCommitHash().substring(0, 7)
                        + " " + c.getMerged().getCommitHash().substring(0, 7));
            }
            System.out.println("Date: " + c.getDateString());
            System.out.println(c.getMessage());
            if (c.getParent() != null) {
                System.out.println();
            }
            c = c.getParent();
        }
    }

    public void gLog() {
        boolean first = true;
        for (String s : _commits.keySet()) {
            Commit c = _commits.get(s);
            if (!first) {
                System.out.println();
            }
            System.out.println("===");
            System.out.println("commit " + c.getCommitHash());
            if (c.getMerged() != null) {
                System.out.println("Merge: " + c.getParent().getCommitHash().substring(0, 7)
                        + " " + c.getMerged().getCommitHash().substring(0, 7));
            }
            System.out.println("Date: " + c.getDateString());
            System.out.println(c.getMessage());
            first = false;
        }
    }

    public void find(String msg) {
        boolean found = false;
        for (String s : _commits.keySet()) {
            Commit c = _commits.get(s);
            if (c.getMessage().equals(msg)) {
                System.out.println(c.getCommitHash());
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    public void status() {
        List<String> all = new ArrayList<>();
        System.out.println("=== Branches ===");
        File branches = new File(".gitlet" + File.separator + "branches");
        List<String> branchList = Utils.plainFilenamesIn(branches);
        if (branchList != null) {
            for (String s : branchList) {
                if (s.equals(currentBranch)) {
                    System.out.println("*" + s);
                } else {
                    System.out.println(s);
                }
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        File stageFiles = new File(".gitlet" + File.separator + "staged");
        List<String> stageList = Utils.plainFilenamesIn(stageFiles);
        if (stageList != null) {
            all.addAll(stageList);
            for (String s : stageList) {
                System.out.println(s);
            }
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        File removeFiles = new File(".gitlet" + File.separator + "removed");
        List<String> removeList = Utils.plainFilenamesIn(removeFiles);
        if (removeList != null) {
            for (String s : removeList) {
                System.out.println(s);
            }
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
//        Commit c1 = _commits.get(head);
//
//        File stagedDir = new File(".gitlet" + File.separator + "staged");
//        List<String> stagedNames = Utils.plainFilenamesIn(stagedDir);
//
//        File removedDir = new File(".gitlet" + File.separator + "removed");
//        List<String> removedNames = Utils.plainFilenamesIn(removedDir);
//        File workDir = new File(".");
//        List<String> workNames = Utils.plainFilenamesIn(workDir);
//        List<String> commitFileNames = c1.getBlobNames();
//        if (removedNames != null) {
//            Collections.sort(removedNames);
//        }
//        if (workNames != null) {
//            Collections.sort(workNames);
//
//        }
//        if (commitFileNames != null) {
//            Collections.sort(commitFileNames);
//        }
//        if (stagedNames != null) {
//            Collections.sort(stagedNames);
//        }
//        if (commitFileNames != null) {
//            for (String fileName : commitFileNames) {
//                File f = new File(fileName);
//                if (workNames != null && workNames.contains(fileName)) {
//                    String fileHash = "blob " + fileName + " " + Utils.readContentsAsString(f);
//                    if (!c1.getBlobHash(fileName).equals(fileHash) && ((stagedNames != null
//                            && !stagedNames.contains(fileName)) || stagedNames == null)) {
//                        System.out.println(fileName + " (modified)");
//                    }
//                } else if (((removedNames != null && !removedNames.contains(fileName)) || removedNames == null)
//                        && !f.exists()) {
//                    System.out.println(fileName + " (deleted)");
//
//                }
//            }
//        }
//        if (stagedNames != null) {
//            for (String fileName : stagedNames) {
//                File f = new File(fileName);
//                File stage = new File(".gitlet" + File.separator + "staged" + File.separator + fileName);
//                if (workNames != null && workNames.contains(fileName)) {
//                    String workHash = "blob " + fileName + " " + Utils.readContentsAsString(f);
//                    String stageHash = "blob " + fileName + " " + Utils.readContentsAsString(stage);
//                    if (!stageHash.equals(workHash)) {
//                        System.out.println(fileName + " (modified)");
//                    }
//                } else if (!f.exists()) {
//                    System.out.println(fileName + " (deleted)");
//                }
//            }
//        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        File untrackedFiles = new File(".");
        List<String> untrackedList = Utils.plainFilenamesIn(untrackedFiles);
        Commit c = _commits.get(head);
        List<String> trackedList = c.getBlobNames();
        if (trackedList != null) {
            all.addAll(trackedList);
        }
        if (untrackedList != null) {
            for (String s1 : untrackedList) {
                if (!all.contains(s1)) {
                    System.out.println(s1);
                }
            }
        }
    }

    public void fileCheckout(String fileName) {
        Commit c = _commits.get(head);
        boolean exists = c.getBlobNames().contains(fileName);
        if (exists) {
            File work = new File(fileName);
            Utils.writeContents(work, c.getContent(fileName));
        } else {
            System.out.println("File does not exist in that commit.");
        }
    }

    public void checkout(String commitID, String fileName) {
        String longID = null;
        Commit c;
        if (commitID.length() < 40) {
            String shortID = commitID.substring(0, 7);
            for (String s : _commits.keySet()) {
                String shortenID = s.substring(0, 7);
                if (shortenID.equals(shortID)) {
                    longID = s;
                    break;
                }
            }
        }
        if (longID != null) {
            c = _commits.get(longID);
        } else {
            c = _commits.get(commitID);
        }
        if (c == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        boolean exists = c.getBlobNames().contains(fileName);
        if (exists) {
            File work = new File(fileName);
            Utils.writeContents(work, c.getContent(fileName));
        } else {
            System.out.println("File does not exist in that commit.");
        }
    }

    public void branchCheckout(String branchName) {
        File branch = new File(".gitlet" + File.separator
                + "branches" + File.separator + branchName);
        if (branch.exists()) {
            if (branchName.equals(currentBranch)) {
                System.out.println("No need to checkout the current branch.");
                return;
            }
            String branchHead = Utils.readContentsAsString(branch);
            File workDir = new File(".");
            List<String> workFiles = Utils.plainFilenamesIn(workDir);
            Commit old = _commits.get(branchHead);
            Commit current = _commits.get(head);
            List<String> oldFiles = old.getBlobNames();
            List<String> currentFiles = current.getBlobNames();
            if (workFiles != null) {
                for (String s : workFiles) {
                    if (currentFiles != null && !currentFiles.contains(s)
                            && oldFiles != null && oldFiles.contains(s)) {
                        System.out.println("There is an untracked file in the way; delete it or add it first.");
                        return;
                    }
                }
            }
            if (oldFiles != null) {
                for (String s : oldFiles) {
                    checkout(branchHead, s);
                }
            }
            if (currentFiles != null) {
                for (String s : currentFiles) {
                    if (oldFiles != null && !oldFiles.contains(s)) {
                        File deleteFile = new File(s);
                        deleteFile.delete();
                    }
                }
            }
            File stageFile = new File(".gitlet" + File.separator + "staged");
            List<String> stagedFiles = Utils.plainFilenamesIn(stageFile);
            if (stagedFiles != null) {
                for (String s : stagedFiles) {
                    File removeFile = new File(".gitlet" + File.separator
                            + "staged" + File.separator + s);
                    removeFile.delete();
                }
            }
            currentBranch = branchName;
            head = branchHead;
        } else {
            System.out.println("No such branch exists.");
        }
    }

    public void branch(String branchName) {
        File addBranch = new File(".gitlet" + File.separator
                + "branches" + File.separator + branchName);
        if (addBranch.exists()) {
            System.out.println("A branch with that name already exists.");
        } else {
            Utils.writeContents(addBranch, head);
            _branches.put(head, branchName);
        }
    }

    public void removeBranch(String branchName) {
        File removeBranch = new File(".gitlet" + File.separator
                + "branches" + File.separator + branchName);
        if (!removeBranch.exists()) {
            System.out.println("A branch with that name does not exist.");
        } else if (branchName.equals(currentBranch)) {
            System.out.println("Cannot remove the current branch.");
        } else {
            removeBranch.delete();
        }
    }

    public void reset(String commitID) {
        String longID = null;
        Commit oldCommit;
        if (commitID.length() < 40) {
            String shortID = commitID.substring(0, 7);
            for (String s : _commits.keySet()) {
                String shortenID = s.substring(0, 7);
                if (shortenID.equals(shortID)) {
                    longID = s;
                    break;
                }
            }
        }
        if (longID != null) {
            oldCommit = _commits.get(longID);
        } else {
            oldCommit = _commits.get(commitID);
        }
        if (oldCommit == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit currentCommit = _commits.get(head);
        File workDir = new File(".");
        List<String> workFiles = Utils.plainFilenamesIn(workDir);
        List<String> oldFiles = oldCommit.getBlobNames();
        List<String> currentFiles = currentCommit.getBlobNames();
        if (workFiles != null) {
            for (String s : workFiles) {
                if (currentFiles != null && !currentFiles.contains(s) && oldFiles != null && oldFiles.contains(s)) {
                    System.out.println("There is an untracked file in the way; delete it or add it first.");
                    return;
                }
            }
        }
        if (oldFiles != null) {
            for (String s : oldFiles) {
                if (longID != null) {
                    checkout(longID, s);
                } else {
                    checkout(commitID, s);
                }
            }
        }
        if (currentFiles != null) {
            for (String s : currentFiles) {
                if (oldFiles != null && !oldFiles.contains(s)) {
                    File deleteFile = new File(s);
                    deleteFile.delete();
                }
            }
        }
        File stageFile = new File(".gitlet" + File.separator + "staged");
        List<String> stagedFiles = Utils.plainFilenamesIn(stageFile);
        if (stagedFiles != null) {
            for (String s : stagedFiles) {
                File removeFile = new File(".gitlet" + File.separator
                        + "staged" + File.separator + s);
                removeFile.delete();
            }
        }
        File branch = new File(".gitlet" + File.separator
                + "branches" + File.separator + currentBranch);
        if (longID != null) {
            head = longID;
            Utils.writeContents(branch, head);
            _branches.put(head, currentBranch);
        } else {
            head = commitID;
            Utils.writeContents(branch, head);
            _branches.put(head, currentBranch);
        }
    }

    public void merge(String branchName) {
        File staged = new File(".gitlet" + File.separator + "staged");
        List<String> s = Utils.plainFilenamesIn(staged);
        File removed = new File(".gitlet" + File.separator + "removed");
        List<String> r = Utils.plainFilenamesIn(removed);
        if (!s.isEmpty() || !r.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        File branchPointer = new File(".gitlet" + File.separator
                + "branches" + File.separator + branchName);
        if (!branchPointer.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (currentBranch.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        // FIXME? merge generates error with commit that has no changes

        File branchFile = new File(".gitlet" + File.separator
                + "branches" + File.separator + branchName);
        String newCommitHash = Utils.readContentsAsString(branchFile);

        Commit newCommit = _commits.get(newCommitHash);
        Commit currCommit = _commits.get(head);

        File w = new File(".");
        List<String> workingDirFileNames = Utils.plainFilenamesIn(w);
        List<String> newCommitFiles = newCommit.getBlobNames();
        List<String> currTracked = currCommit.getBlobNames();

        if (workingDirFileNames != null) {
            for (String f : workingDirFileNames) {
                if (currTracked != null && !currTracked.contains(f) && newCommitFiles != null && newCommitFiles.contains(f)) {
                    System.out.println("There is an untracked file in the way; delete it or add it first.");
                    System.exit(0);
                }
            }
        }

        // if the split point is the same commit as the given branch, then we do nothing; the merge is complete,
        // and the operation ends with the message
        // Given branch is an ancestor of the current branch.
        String splitSha1 = splitPoint(currCommit, newCommit);
        Commit splitCommit = _commits.get(splitSha1);
        File givenBranchFile = new File(".gitlet" + File.separator
                + "branches" + File.separator + branchName);
        String givenBranchHash = Utils.readContentsAsString(givenBranchFile);
        Commit givenBranch = _commits.get(givenBranchHash);
        if (givenBranchHash.equals(splitSha1)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }


//        If the split point is the current branch, then the current branch is set to the same commit
// as the given branch and the operation ends after printing the message Current branch fast-forwarded.
// Otherwise, we continue with the steps below.
        File currBranchFile = new File(".gitlet" + File.separator
                + "branches" + File.separator + currentBranch);
        String currBranch = Utils.readContentsAsString(currBranchFile);
        if (splitSha1.equals(currBranch)) {
            Utils.writeContents(currBranchFile, givenBranchHash);
            head = givenBranchHash;
            System.out.println("Current branch fast-forwarded.");
            return;
        }

//        "modified in the given branch since the split point"

//        Any files that have been modified in the given branch since the split point, but not modified in the
// current branch since the split point should be changed to their versions in the given branch
// (checked out from the commit at the front of the given branch). These files should then all be automatically staged.
// To clarify, if a file is "modified in the given branch since the split point" this means the version of the file
// as it exists in the commit at the front of the given branch has different content from the version of the file
// at the split point.
        List<String> givenBranchFiles = givenBranch.getBlobNames();
        List<String> splitCommitFiles = splitCommit.getBlobNames();
        for (String f : splitCommitFiles) {
            if (givenBranch.getBlobNames().contains(f) && splitCommit.getBlobNames().contains(f)
                    && currCommit.getBlobNames().contains(f)
                    && !givenBranch.getBlobHash(f).equals(splitCommit.getBlobHash(f))
                    && (currCommit.getBlobHash(f).equals(splitCommit.getBlobHash(f)))) {
                //System.out.println("Making it here");
                checkout(givenBranchHash, f);
                File stagedF = new File(".gitlet" + File.separator + "staged" + File.separator + f);
                File file = new File(f);
                Utils.writeContents(file, givenBranch.getContent(f));
                Utils.writeContents(stagedF, givenBranch.getContent(f));
            }
        }


//        Any files that have been modified in the current branch but not in the given branch since the split point
// should stay as they are.
        for (String f : splitCommitFiles) {
            if (givenBranch.getBlobNames().contains(f) && splitCommit.getBlobNames().contains(f)
                    && currCommit.getBlobNames().contains(f)
                    && givenBranch.getBlobHash(f).equals(splitCommit.getBlobHash(f))
                    && !currCommit.getBlobHash(f).equals(splitCommit.getBlobHash(f))) {
                File file = new File(f);
                Utils.writeContents(file, currCommit.getContent(f));
            }
        }

//        Any files that have been modified in both the current and given branch in the same way
// (i.e., both to files with the same content or both removed) are left unchanged by the merge.
// In particular, if a file is removed in both, but a file of that name is present in the working directory,
// that file is not removed from the working directory.
        for (String f : splitCommitFiles) {
            if (splitCommit.getBlobNames().contains(f)
                    && givenBranch.getBlobNames().contains(f)
                    && currCommit.getBlobNames().contains(f)
                    && !splitCommit.getBlobHash(f).equals(givenBranch.getBlobHash(f))
                    && !splitCommit.getBlobHash(f).equals(currCommit.getBlobHash(f))
                    && currCommit.getBlobHash(f).equals(givenBranch.getBlobHash(f))) {
                File file = new File(f);
                Utils.writeContents(file, currCommit.getContent(f));
            }
        }
        File work = new File(".");
        List<String> workFiles = Utils.plainFilenamesIn(work);
        for (String f : splitCommitFiles) {
            if (splitCommit.getBlobNames().contains(f)
                    && !givenBranch.getBlobNames().contains(f)
                    && !currCommit.getBlobNames().contains(f)
                    && workFiles != null && workFiles.contains(f)) {
                File file = new File(f);
                Utils.writeContents(file, Utils.readContents(file));
            }
        }
//        Any files that were not present at the split point and are present only in the current branch should
// remain as they are.
        for (String f : currCommit.getBlobNames()) {
            if (!splitCommit.getBlobNames().contains(f) && !givenBranch.getBlobNames().contains(f)
                    && currCommit.getBlobNames().contains(f)) {
                File file = new File(f);
                Utils.writeContents(file, currCommit.getContent(f));
            }
        }

//        Any files that were not present at the split point and are present only in the given branch should
// be checked out and staged.
        for (String f : givenBranchFiles) {
            if (!splitCommit.getBlobNames().contains(f) && givenBranch.getBlobNames().contains(f)
                    && !currCommit.getBlobNames().contains(f)) {
                checkout(givenBranch.getCommitHash(), f);
                File stagedF = new File(".gitlet" + File.separator + "staged" + File.separator + f);
                Utils.writeContents(stagedF, givenBranch.getContent(f));
            }
        }


//        Any files present at the split point, unmodified in the current branch,
// and absent in the given branch should be removed (and untracked).
        for (String f : splitCommitFiles) {
            if (splitCommit.getBlobNames().contains(f) && currCommit.getBlobNames().contains(f)
                    && !givenBranch.getBlobNames().contains(f)
                    && splitCommit.getBlobHash(f).equals(currCommit.getBlobHash(f))) {
                File file = new File(f);
                File removeFile = new File(".gitlet" + File.separator + "removed" + File.separator + f);
                Utils.writeContents(removeFile, currCommit.getContent(f));
                file.delete();
            }
        }


//        Any files present at the split point, unmodified in the given branch, and absent in the current branch
// should remain absent.


//        "Modified in different ways" can mean that the contents of both are changed and different from other,
// or the contents of one are changed and the other is deleted, or the file was absent at the split point and have
// different contents in the given and current branches.
        TreeSet<String> all = new TreeSet<>();
        all.addAll(currCommit.getBlobNames()); all.addAll(givenBranch.getBlobNames());
        all.addAll(splitCommit.getBlobNames());
        boolean conflict = false;
        for (String f : all) {
            if (newCommit.getBlobNames().contains(f) && currCommit.getBlobNames().contains(f)
                    && splitCommit.getBlobNames().contains(f)
                    && (!currCommit.getBlobHash(f).equals(newCommit.getBlobHash(f)))
                    && (!currCommit.getBlobHash(f).equals(splitCommit.getBlobHash(f)))
                    && (!newCommit.getBlobHash(f).equals(splitCommit.getBlobHash(f)))) {
                conflict = true;
                writeConflictingContents(currCommit, givenBranch, f);

            } else if (currCommit.getBlobNames().contains(f) && splitCommit.getBlobNames().contains(f)
                    && !newCommit.getBlobNames().contains(f)
                    && (!currCommit.getBlobHash(f).equals(splitCommit.getBlobHash(f)))) {
                conflict = true;
                writeConflictingContents(currCommit, givenBranch, f);
            } else if (newCommit.getBlobNames().contains(f) && splitCommit.getBlobNames().contains(f)
                    && !currCommit.getBlobNames().contains(f)
                    && (!newCommit.getBlobHash(f).equals(splitCommit.getBlobHash(f)))) {
                conflict = true;
                writeConflictingContents(currCommit, givenBranch, f);
            } else if (newCommit.getBlobNames().contains(f) && currCommit.getBlobNames().contains(f)
                    && !splitCommit.getBlobNames().contains(f)
                    && (!newCommit.getBlobHash(f).equals(currCommit.getBlobHash(f)))) {
                conflict = true;
                writeConflictingContents(currCommit, givenBranch, f);
            }
        }

//        for (String f : newCommit.getBlobNames()) {
//            if (newCommit.getBlobNames().contains(f) && currCommit.getBlobNames().contains(f)
//                    && splitCommit.getBlobNames().contains(f)
//                    && (!currCommit.getBlobHash(f).equals(newCommit.getBlobHash(f)))
//                    && (!currCommit.getBlobHash(f).equals(splitCommit.getBlobHash(f)))
//                    && (!newCommit.getBlobHash(f).equals(splitCommit.getBlobHash(f)))) {
//                conflict = true;
//                writeConflictingContents(currCommit, givenBranch, f);
//            } else if (currCommit.getBlobNames().contains(f) && splitCommit.getBlobNames().contains(f)
//                    && (!currCommit.getBlobHash(f).equals(splitCommit.getBlobHash(f)))
//                    && !newCommit.getBlobNames().contains(f)) {
//                conflict = true;
//                writeConflictingContents(currCommit, givenBranch, f);
//            } else if (newCommit.getBlobNames().contains(f) && splitCommit.getBlobNames().contains(f)
//                    && (!newCommit.getBlobHash(f).equals(splitCommit.getBlobHash(f)))
//                    && !currCommit.getBlobNames().contains(f)) {
//                conflict = true;
//                writeConflictingContents(currCommit, givenBranch, f);
//            } else if (newCommit.getBlobNames().contains(f) && currCommit.getBlobNames().contains(f)
//                    && (!newCommit.getBlobHash(f).equals(currCommit.getBlobHash(f)))
//                    && !splitCommit.getBlobNames().contains(f) ) {
//                conflict = true;
//                writeConflictingContents(currCommit, givenBranch, f);
//            }
//        }
        File stageFile = new File(".gitlet" + File.separator + "staged");
        File removeFile = new File(".gitlet" + File.separator + "removed");
        if (Utils.plainFilenamesIn(stageFile) != null
                && Utils.plainFilenamesIn(stageFile).size() == 0
                && Utils.plainFilenamesIn(removeFile) != null
                && Utils.plainFilenamesIn(removeFile).size() == 0) {
            System.out.println("No changes added to the commit.");
            return;
        }
        Commit a = new Commit(currCommit, "Merged "
                + branchName + " into " + currentBranch + ".",
                newCommit);
        a.addParentBlobs();
        a.stageFiles();
        a.removeFiles();
        a.setCommitHash();
        head = a.getCommitHash();
        _branches.put(head, currentBranch);
        _commits.put(head, a);

        // the branch name holds the sha1 value of the commit it points to
        File masterBranch = new File(".gitlet" + File.separator
                + "branches" + File.separator + currentBranch);
        Utils.writeContents(masterBranch, head);
        File otherBranch = new File(".gitlet" + File.separator
                + "branches" + File.separator + branchName);
        Utils.writeContents(otherBranch, head);

        // writes the commit object to a file where it's name is it's sha1 value
        File commitFile = new File(".gitlet" + File.separator
                + "commits" + File.separator + head);
        Utils.writeObject(commitFile, a);

        //commit("Merged " + givenBranchHash.substring(0, 7) + " into " + currBranch.substring(0, 7) + ".");

        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }



    }

    public void writeConflictingContents(Commit currCommit, Commit givenBranch, String f) {
        File file = new File(f);
        File stagedF = new File(".gitlet" + File.separator
                + "staged" + File.separator + f);
        if (!currCommit.getBlobNames().contains(f)) {
            Utils.writeContents(file, "<<<<<<< HEAD\n"
                    + ""
                    + "=======\n"
                    + givenBranch.getContent(f)
                    + ">>>>>>>");
        } else if (!givenBranch.getBlobNames().contains(f)) {
            Utils.writeContents(file, "<<<<<<< HEAD\n"
                    + currCommit.getContent(f)
                    + "=======\n"
                    + ""
                    + ">>>>>>>");
        } else {
            Utils.writeContents(file, "<<<<<<< HEAD\n"
                    + currCommit.getContent(f)
                    + "=======\n"
                    + givenBranch.getContent(f)
                    + ">>>>>>>");
        }
        Utils.writeContents(stagedF, Utils.readContents(file));
//        if (currCommit.getBlobNames().contains(f)) {
//            currCommit.getBlobContent().put(f, Utils.readContentsAsString(file));
//        }
        // If conflicted file exists in both
//        if (givenBranch.getFileContents().containsKey(f)) {
//            givenBranch.getFileContents().put(f, Utils.readContents(file));
//        }
    }

    public static String splitPoint(Commit c1, Commit c2) {
        while (c1.getParent() != null || c2.getParent() != null) {
            if (!c1.getCommitHash().equals(c2.getCommitHash())) {
                if (c1.getDate().before(c2.getDate())) {
                    c2 = c2.getParent();
                } else {
                    c1 = c1.getParent();
                }
            } else {
                return c1.getCommitHash();
            }
        }
        if (c1.getParent() == null) {
            return c1.getCommitHash();
        } else {
            return c2.getCommitHash();
        }
    }

    /** Maps commit's hash value to its corresponding commit. */
    private HashMap<String, Commit> _commits = new HashMap<>();

    /** Maps head to it's branch name. */
    private HashMap<String, String> _branches = new HashMap<>();

    /** The branch of the current commit. */
    private String currentBranch;

    /** The current commit's hash value. */
    private String head;
}
