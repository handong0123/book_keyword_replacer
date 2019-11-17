package com.github.handong0123;

import com.github.handong0123.epub.EpubReplacer;
import com.github.handong0123.pdf.PdfReplacer;
import com.github.handong0123.txt.TxtReplacer;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class App {
    private JButton buttonStart;
    private JTextField textPath;
    private JTextField textStartNo;
    private JTextField textOld1;
    private JTextField textNew1;
    private JTextField textOld2;
    private JTextField textNew2;
    private JTextField textOld3;
    private JTextField textNew3;
    private JTextField textOld4;
    private JTextField textNew4;
    private JPanel panelWindow;
    private JPanel panelButton;
    private JPanel panelPathChoose;
    private JPanel panelStartNo;
    private JPanel panelText;
    private JPanel panelReplace1;
    private JPanel panelReplace2;
    private JPanel panelReplace3;
    private JPanel panelReplace4;
    private JButton buttonFileChooser;


    public App() {
        buttonFileChooser.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int option = fileChooser.showOpenDialog(panelWindow);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                textPath.setText(file.getAbsolutePath());
            }
        });
        buttonStart.addActionListener(e -> {
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
            int start;
            try {
                start = Integer.parseInt(startNo);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "开始序号请填写数字", "提醒", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int noLength = startNo.length();
            Map<String, String> replaceMap = new HashMap<>();
            if (StringUtils.isNotBlank(textOld1.getText())) {
                replaceMap.put(textOld1.getText(), textNew1.getText());
            }
            if (StringUtils.isNotBlank(textOld2.getText())) {
                replaceMap.put(textOld2.getText(), textNew2.getText());
            }
            if (StringUtils.isNotBlank(textOld3.getText())) {
                replaceMap.put(textOld3.getText(), textNew3.getText());
            }
            if (StringUtils.isNotBlank(textOld4.getText())) {
                replaceMap.put(textOld4.getText(), textNew4.getText());
            }
            File[] directories = workPath.listFiles();
            if (null == directories || directories.length == 0) {
                JOptionPane.showMessageDialog(null, "空文件夹", "提醒", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String newWordPath = workPath.getAbsolutePath() + "_new";
            new File(newWordPath).mkdir();
            for (File directory : directories) {
                if (directory.isFile()) {
                    continue;
                }
                File[] files = directory.listFiles();
                if (null == files || files.length == 0) {
                    continue;
                }
                String directoryName = String.format("%0" + noLength + "d", start) + directory.getName();
                start++;
                File newDirectory = new File(newWordPath, directoryName);
                if(!newDirectory.mkdir()){
                    continue;
                }
                for (File f : files) {
                    if (f.isDirectory()) {
                        continue;
                    }
                    String fileName = f.getName();
                    if (fileName.toLowerCase().endsWith(".txt")) {
                        TxtReplacer.replace(f.getAbsolutePath(), newDirectory.getAbsolutePath() + "/" + fileName, replaceMap);
                    } else if (fileName.toLowerCase().endsWith(".epub")) {
                        EpubReplacer.replace(f.getAbsolutePath(), newDirectory.getAbsolutePath() + "/" + fileName, replaceMap);
                    } else if (fileName.toLowerCase().endsWith(".pdf")) {
                        PdfReplacer.replace(f.getAbsolutePath(), newDirectory.getAbsolutePath() + "/" + fileName, replaceMap);
                    }
                }
            }
            JOptionPane.showMessageDialog(null, "替换完成", "提醒", JOptionPane.WARNING_MESSAGE);
        });
    }

    public static void main(String[] args) {
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
        long expired = 1574092799000L;
        if (System.currentTimeMillis() > expired) {
            JOptionPane.showMessageDialog(null, "软件已过期", "提醒", JOptionPane.WARNING_MESSAGE);
            System.exit(1);
        }
    }
}
