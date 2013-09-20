package org.xendan.logmonitor.idea;

import org.xendan.logmonitor.model.Server;
import org.xendan.logmonitor.read.LsCommand;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class ServerLogChooser extends JDialog {
    private final LogChooseListener listener;
    private final LsCommand lsCommand;
    private final DefaultTreeModel treeModel;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTree fileTree;
    private String path;

    public ServerLogChooser(Server server, LogChooseListener listener) {
        this.listener = listener;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        lsCommand = new LsCommand(server);
        FileNode root = new FileNode("", null, true);
        root.loadFiles();
        treeModel = new DefaultTreeModel(root);
        fileTree.setModel(treeModel);
        fileTree.addTreeSelectionListener(new FileTreeSelectionListener());
        fileTree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);
        fileTree.addTreeExpansionListener(new FileTreeExpansionListener());
    }

    private void onOK() {
        listener.onFileSelected(path);
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    private class FileNode implements MutableTreeNode {
        public static final String ROOT_PATH = "/";
        private String name;
        private FileNode parent;
        private final boolean isDir;
        private List<TreeNode> children = new ArrayList<TreeNode>();
        private boolean loaded;
        private final DefaultMutableTreeNode loading = new DefaultMutableTreeNode("Loading...");
        private final DefaultMutableTreeNode errorLoading = new DefaultMutableTreeNode("Error connecting [" + lsCommand.getConnectionStr() + "]...");

        public FileNode(String name, FileNode parent, boolean isDir) {
            this.name = name;
            this.parent = parent;
            this.isDir = isDir;
            if (isDir) {
                insertChild(loading);
            }
        }

        @Override
        public TreeNode getChildAt(int childIndex) {
            return children.get(childIndex);
        }

        @Override
        public int getChildCount() {
            return children.size();
        }

        public void loadFiles() {
            if (!loaded && isDir) {
                loaded = true;
                Thread lsTask = new LsTask(this);
                lsTask.start();
            }
        }

        @Override
        public FileNode getParent() {
            return parent;
        }

        @Override
        public int getIndex(TreeNode node) {
            return children.indexOf(node);
        }

        @Override
        public boolean getAllowsChildren() {
            return isDir;
        }

        @Override
        public boolean isLeaf() {
            return !isDir;
        }

        private String getPath() {
            String path = ROOT_PATH + name;
            path = (parent == null ? "" : parent.getPath()) + path;
            if (path.startsWith("//")) {
                path = path.replace("//", ROOT_PATH);
            }
            return path;
        }

        @Override
        public Enumeration children() {
            return Collections.enumeration(children);
        }

        @Override
        public String toString() {
            if (parent == null) {
                return ROOT_PATH;
            }
            return name;
        }

        public void setFilesAndDirs(final String[][] filesAndDirs) {
            treeModel.removeNodeFromParent(loading);
            if (filesAndDirs == null) {
                insertChild(errorLoading);
            } else {
                for (String dir : filesAndDirs[0]) {
                    insertChild(new FileNode(dir, FileNode.this, true));
                }
                for (String file : filesAndDirs[1]) {
                    insertChild(new FileNode(file, FileNode.this, false));
                }
            }
        }

        private void insertChild(final MutableTreeNode node) {
            node.setParent(this);
            if (treeModel != null) {
                treeModel.insertNodeInto(node, FileNode.this, children.size());
            } else {
                children.add(node);
            }
        }

        @Override
        public void insert(MutableTreeNode child, int index) {
            children.add(index, child);
        }

        @Override
        public void remove(int index) {
            if (index >= 0 && index < children.size()) {
                children.remove(index);
            }
        }

        @Override
        public void remove(MutableTreeNode node) {
            children.remove(node);
        }

        @Override
        public void setUserObject(Object object) {
        }

        @Override
        public void removeFromParent() {
            parent.remove(this);
        }

        @Override
        public void setParent(MutableTreeNode newParent) {
            if (newParent instanceof FileNode) {
                parent = (FileNode) newParent;
            }
        }
    }


    private class FileTreeSelectionListener implements TreeSelectionListener {
        @Override
        public void valueChanged(TreeSelectionEvent e) {
            TreePath treePath = e.getPath();
            Object last = treePath.getLastPathComponent();
            if (last instanceof FileNode) {
                path = ((FileNode) last).getPath();
            }
        }
    }

    private class FileTreeExpansionListener implements TreeExpansionListener {
        @Override
        public void treeExpanded(TreeExpansionEvent event) {
            Object last = event.getPath().getLastPathComponent();
            if (last instanceof FileNode) {
                ((FileNode) last).loadFiles();
            }
        }

        @Override
        public void treeCollapsed(TreeExpansionEvent event) {
        }
    }

    private class LsTask extends Thread {
        private final FileNode dirNode;

        public LsTask(FileNode dirNode) {
            this.dirNode = dirNode;
        }

        @Override
        public void run() {
            final String[][] dirsAndFiles = lsCommand.getDirsAndFiles(dirNode.getPath());
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    dirNode.setFilesAndDirs(dirsAndFiles);
                }
            });
        }
    }
}
