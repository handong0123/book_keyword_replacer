package com.github.handong0123;

import com.github.handong0123.epub.EpubReplacer;
import com.github.handong0123.pdf.PdfReplacer;
import com.github.handong0123.txt.TxtReplacer;
import com.github.handong0123.word.DocReplacer;
import com.github.handong0123.word.DocxReplacer;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * swing界面
 *
 * @author handong0123
 */
public class App {
    private JButton buttonStart;
    private JTextField textPath;
    private JTextField textStartNo;
    private JPanel panelWindow;
    private JPanel panelButton;
    private JPanel panelPathChoose;
    private JPanel panelStartNo;
    private JPanel panelText;
    private JButton buttonFileChooser;
    private JTextArea textAreaReplace;
    private JProgressBar progressBar1;
    private JButton buttonStop;
    private JList listResult;
    private JLabel labelStatus;

    private ThreadTask thread;

    public App() {
        buttonFileChooser.addActionListener(e -> {
            // 路径选择器
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int option = fileChooser.showOpenDialog(panelWindow);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                textPath.setText(file.getAbsolutePath());
            }
        });
        buttonStart.addActionListener(e -> {
            // 关闭开始按钮
            buttonStart.setEnabled(false);
            // 清空失败列表
            listResult.setListData(new Object[0]);
            String filePath = textPath.getText();
            String startNo = textStartNo.getText();
            if (StringUtils.isBlank(filePath)) {
                JOptionPane.showMessageDialog(null, "请选择文件夹路径", "提醒", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (StringUtils.isBlank(startNo)) {
                JOptionPane.showMessageDialog(null, "请填写开始序号", "提醒", JOptionPane.WARNING_MESSAGE);
                return;
            }
            File workPath = new File(filePath);
            if (!workPath.isDirectory()) {
                JOptionPane.showMessageDialog(null, "请选择文件夹,非文件", "提醒", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // 解析命名起始序号
            int start;
            try {
                start = Integer.parseInt(startNo);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "开始序号请填写数字", "提醒", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int noLength = startNo.length();
            // 解析替换词典
            Map<String, String> replaceMap = new HashMap<>();
            String[] items = textAreaReplace.getText().split("\n");
            for (String item : items) {
                String[] pair = item.split("->");
                if (pair.length != 2) {
                    continue;
                }
                replaceMap.put(pair[0], pair[1]);
            }
            File[] directories = workPath.listFiles();
            if (null == directories || directories.length == 0) {
                JOptionPane.showMessageDialog(null, "空文件夹", "提醒", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // 开始替换任务线程
            thread = new ThreadTask(workPath, directories, noLength, start, replaceMap);
            thread.start();
        });
        buttonStop.addActionListener(e -> thread.setStop(true));
    }

    public static void main(String[] args) {
        // 设置UI
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("文件内容关键词替换器");
        frame.setContentPane(new App().panelWindow);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * 迭代删除文件夹
     *
     * @param dirPath 文件夹路径
     */
    private static void deleteDir(String dirPath) {
        File file = new File(dirPath);
        if (file.isFile()) {
            file.delete();
        } else {
            File[] files = file.listFiles();
            if (files == null) {
                file.delete();
            } else {
                for (int i = 0; i < files.length; i++) {
                    deleteDir(files[i].getAbsolutePath());
                }
                file.delete();
            }
        }
    }


    /**
     * 替换任务线程
     */
    class ThreadTask extends Thread {

        private File workPath;
        private File[] directories;
        private int noLength;
        private int start;
        private Map<String, String> replaceMap;

        /**
         * 标记变量可见性，从主存中读取变量最新值
         */
        private volatile boolean stop = false;

        public void setStop(boolean stop) {
            this.stop = stop;
        }

        List<String> failedList = new ArrayList<>();

        ThreadTask(File workPath, File[] directories, int noLength, int start, Map<String, String> replaceMap) {
            this.workPath = workPath;
            this.directories = directories;
            this.noLength = noLength;
            this.start = start;
            this.replaceMap = replaceMap;
        }

        @Override
        public void run() {
            String newWordPath = workPath.getAbsolutePath() + "_new";
            File root = new File(newWordPath);
            if (!root.mkdirs()) {
                deleteDir(newWordPath);
                root.mkdirs();
            }
            // 设置进度条
            progressBar1.setMaximum(directories.length);
            progressBar1.setMinimum(0);
            for (int i = 0; i < directories.length; i++) {
                progressBar1.setValue(i);
                File directory = directories[i];
                if (directory.isFile()) {
                    continue;
                }
                File[] files = directory.listFiles();
                if (null == files || files.length == 0) {
                    continue;
                }
                String directoryName = String.format("%0" + noLength + "d", start) + "-" + directory.getName();
                start++;
                File newDirectory = new File(newWordPath, directoryName);
                if (!newDirectory.mkdir()) {
                    continue;
                }
                //遍历文件，符合格式就进行替换
                for (File f : files) {
                    // 停止标记
                    if (stop) {
                        progressBar1.setIndeterminate(false);
                        JOptionPane.showMessageDialog(null, "任务已终止", "提醒", JOptionPane.WARNING_MESSAGE);
                        buttonStart.setEnabled(true);
                        return;
                    }
                    if (f.isDirectory()) {
                        continue;
                    }
                    labelStatus.setText("正在处理:" + f.getName());
                    String fileName = f.getName();
                    boolean flag = true;
                    if (fileName.toLowerCase().endsWith(".txt")) {
                        flag = TxtReplacer.replace(f.getAbsolutePath(), newDirectory.getAbsolutePath() + "/" + fileName, replaceMap);
                    } else if (fileName.toLowerCase().endsWith(".epub")) {
                        flag = EpubReplacer.replace(f.getAbsolutePath(), newDirectory.getAbsolutePath() + "/" + fileName, replaceMap);
                    } else if (fileName.toLowerCase().endsWith(".pdf")) {
                        flag = PdfReplacer.replace(f.getAbsolutePath(), newDirectory.getAbsolutePath() + "/" + fileName, replaceMap);
                    } else if (fileName.toLowerCase().endsWith(".doc")) {
                        flag = DocReplacer.replace(f.getAbsolutePath(), newDirectory.getAbsolutePath() + "/" + fileName, replaceMap);
                    } else if (fileName.toLowerCase().endsWith(".docx")) {
                        flag = DocxReplacer.replace(f.getAbsolutePath(), newDirectory.getAbsolutePath() + "/" + fileName, replaceMap);
                    }
                    // 处理失败，添加到失败列表，该文件夹后续处理跳过
                    if (!flag) {
                        failedList.add(directory.getName());
                        listResult.setListData(failedList.toArray(new String[0]));
                        break;
                    }
                }
            }
            // 重置进度条
            progressBar1.setValue(0);
            JOptionPane.showMessageDialog(null, "替换完成", "提醒", JOptionPane.WARNING_MESSAGE);
            // 恢复开始按钮
            buttonStart.setEnabled(true);
        }
    }
}
