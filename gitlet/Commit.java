package gitlet;

/** The commit class that handles the data of all the commit objects. 
*   @author Mudabbr Khan
*/

import java.io.File;
import java.io.Serializable;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Commit implements Serializable {

    private Commit _merged;

    public Commit getMerged() {
        return _merged;
    }

    public Commit(Commit parent, String message, Commit merged) {
        _parent = parent;
        _message = message;
        _merged = merged;

        Format dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        if (parent == null) {
            date = new Date(0);
            dateString = dateFormat.format(date);
        } else {
            date = new Date();
            dateString = dateFormat.format(date);
        }
        blobs = new HashMap<>();
        blobNames = new ArrayList<>();
        blobContent = new HashMap<>();
    }

    public String getBlobHash(String fileName) {
        return blobs.get(fileName);
    }

    public String getDateString() {
        return dateString;
    }

    public Date getDate() {
        return date;
    }

    public String getMessage() {
        return _message;
    }

    public Commit getParent() {
        return _parent;
    }

    /** Sets the ID for commit. */
    public void setCommitHash() {
        iD = Utils.sha1(createCommitHash());
    }

    /** Returns the ID for commit. */
    public String getCommitHash() {
        return iD;
    }

    /** Creates and returns unique hashing value for commit. */
    public String createCommitHash() {
        String hash = "commit " + _parent + " " + _message + " " + dateString;
        StringBuilder result = new StringBuilder(hash);
        for (String s : blobs.values()) {
            result.append(" ");
            result.append(s);
        }
        return result.toString();
    }

    public String BlobHash(String fileName) {
        String hash = "blob " + fileName + " " + blobContent.get(fileName);
        return Utils.sha1(hash);
    }

    /** Adds all parent's files to tracking. */
    public void addParentBlobs() {
        blobNames.addAll(_parent.blobNames);
        blobContent.putAll(_parent.blobContent);
        blobs.putAll(_parent.blobs);
    }

    public void stageFiles() {
        File stageDir = new File(".gitlet" + File.separator + "staged");
        List<String> stageFileNames = Utils.plainFilenamesIn(stageDir);
        if (stageFileNames != null) {
            for (String file : stageFileNames) {
                File stageFile = new File(".gitlet" + File.separator + "staged" + File.separator + file);
                String readFile = Utils.readContentsAsString(stageFile);
                blobContent.put(file, readFile);
                String blobHash = BlobHash(file);
                blobs.put(file, blobHash);
                blobNames.add(file);
                stageFile.delete();
            }
        }
    }

    public void removeFiles() {
        File removeDir = new File(".gitlet" + File.separator + "removed");
        List<String> removeFileNames = Utils.plainFilenamesIn(removeDir);
        if (removeFileNames != null) {
            for (String file : removeFileNames) {
                File removeFile = new File(".gitlet" + File.separator + "removed" + File.separator + file);
                blobContent.remove(file);
                blobs.remove(file);
                blobNames.remove(file);
                removeFile.delete();
            }
        }
    }

    /** Returns the content from file using FILENAME. */
    public String getContent(String fileName) {
        return blobContent.get(fileName);
    }

    public HashMap<String, String> getBlobContent() {
        return blobContent;
    }

    public List<String> getBlobNames() {
        return blobNames;
    }

    public HashMap<String, String> getBlobs() {
        return blobs;
    }

    /** Previous commit. */
    private Commit _parent;

    /** Commit message. */
    private String _message;

    /** Maps file names to their respective hash values. */
    private HashMap<String, String> blobs;

    /** List of all the file names. */
    private List<String> blobNames;

    /** Maps file name to its content. */
    private HashMap<String, String> blobContent;

    /** Commit's hash value. */
    private String iD;

    /** Commit date. */
    private Date date;

    /** String representation of the commit date. */
    private String dateString;
}
