package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;

// Clase para representar los nodos del árbol
class ExpressionNode {
    String value;
    ExpressionNode left, right;

    ExpressionNode(String value) {
        this.value = value;
        left = right = null;
    }
}

public class ExpressionTreeGUI extends JFrame {

    private JTextField inputField;
    private JButton generateButton;
    private JTextArea resultArea;

    public ExpressionTreeGUI() {
        // Configuración básica de la ventana
        setTitle("Generador de Árbol de Expresión Aritmética");
        setSize(400, 400); // Ajusta el tamaño para que quepa todo
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Campo de texto para ingresar la expresión
        inputField = new JTextField();
        inputField.setPreferredSize(new Dimension(200, 30));

        // Botón para generar el árbol
        generateButton = new JButton("Generar Árbol");
        generateButton.addActionListener(new GenerateButtonListener());

        // Área para mostrar el resultado
        resultArea = new JTextArea();
        resultArea.setEditable(false);

        // Panel para contener el campo de texto y el botón
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());
        topPanel.add(new JLabel("Expresión:"));
        topPanel.add(inputField);
        topPanel.add(generateButton);

        // Añadir componentes a la ventana
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);
    }

    // Acción cuando se pulsa el botón de generar
    private class GenerateButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String expression = inputField.getText();
            if (expression.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Por favor ingrese una expresión aritmética.");
                return;
            }

            try {
                ExpressionTree tree = new ExpressionTree();
                // Convertimos la expresión infija a postfija
                String postfix = tree.infixToPostfix(expression);
                // Construimos el árbol de expresión
                ExpressionNode root = tree.constructTree(postfix);

                // Mostrar el árbol en un cuadro de diálogo
                String treeStr = tree.printTree(root, "", true);

                // Realizar los recorridos
                String preOrder = "Recorrido Preorden: " + tree.preOrderTraversal(root);
                String inOrder = "Recorrido Inorden: " + tree.inOrderTraversal(root);
                String postOrder = "Recorrido Postorden: " + tree.postOrderTraversal(root);

                // Mostrar todo en el área de resultado
                resultArea.setText(treeStr + "\n" + preOrder + "\n" + inOrder + "\n" + postOrder);

                JOptionPane.showMessageDialog(null, treeStr, "Árbol de Expresión", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error al procesar la expresión: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ExpressionTreeGUI gui = new ExpressionTreeGUI();
            gui.setVisible(true);
        });
    }
}

// Clase para manejar la construcción del árbol de expresión
class ExpressionTree {

    // Verifica si un carácter es un operador
    private boolean isOperator(String c) {
        return c.equals("+") || c.equals("-") || c.equals("*") || c.equals("/") || c.equals("^") || c.equals("=");
    }

    // Construir árbol a partir de la notación postfija
    public ExpressionNode constructTree(String postfix) {
        Stack<ExpressionNode> stack = new Stack<>();
        String[] tokens = postfix.split(" ");

        for (String token : tokens) {
            if (!isOperator(token)) {
                stack.push(new ExpressionNode(token));
            } else {
                ExpressionNode node = new ExpressionNode(token);
                node.right = stack.pop();
                node.left = stack.pop();
                stack.push(node);
            }
        }
        return stack.peek();
    }

    // Convertir expresión infija a postfija utilizando el algoritmo Shunting Yard
    public String infixToPostfix(String expression) {
        StringBuilder result = new StringBuilder();
        Stack<String> stack = new Stack<>();
        char[] tokens = expression.toCharArray();

        StringBuilder operand = new StringBuilder(); // Para números de más de un dígito

        for (int i = 0; i < tokens.length; i++) {
            char token = tokens[i];

            // Si es un operando (número, letra o constante e), añádelo al resultado
            if (Character.isDigit(token) || Character.isLetter(token)) {
                operand.append(token);  // Acumular números de más de un dígito
            } else {
                // Si ya tenemos un operando, agregarlo al resultado
                if (operand.length() > 0) {
                    result.append(operand.toString()).append(" ");
                    operand.setLength(0); // Limpiar el acumulador
                }
                // Si es un paréntesis de apertura, apílalo
                if (token == '(') {
                    stack.push(String.valueOf(token));
                }
                // Si es un paréntesis de cierre, desapila hasta encontrar el paréntesis de apertura
                else if (token == ')') {
                    while (!stack.isEmpty() && !stack.peek().equals("(")) {
                        result.append(stack.pop()).append(" ");
                    }
                    stack.pop(); // Elimina '('
                }
                // Si es un operador
                else if (isOperator(String.valueOf(token))) {
                    while (!stack.isEmpty() && precedence(String.valueOf(token)) <= precedence(stack.peek())) {
                        result.append(stack.pop()).append(" ");
                    }
                    stack.push(String.valueOf(token));
                }
            }
        }

        // Agregar cualquier operando que haya quedado
        if (operand.length() > 0) {
            result.append(operand.toString()).append(" ");
        }

        // Desapila los operadores restantes
        while (!stack.isEmpty()) {
            result.append(stack.pop()).append(" ");
        }

        return result.toString().trim();
    }

    // Definir la precedencia de operadores
    private int precedence(String operator) {
        switch (operator) {
            case "=":
                return 0; // El operador de menor precedencia
            case "+":
            case "-":
                return 1;
            case "*":
            case "/":
                return 2;
            case "^":
                return 3;
            default:
                return -1;
        }
    }

    // Método para imprimir el árbol de expresión de manera jerárquica
    public String printTree(ExpressionNode node, String prefix, boolean isLeft) {
        if (node == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append(isLeft ? "├── " : "└── ");
        sb.append(node.value).append("\n");

        sb.append(printTree(node.left, prefix + (isLeft ? "│   " : "    "), true));
        sb.append(printTree(node.right, prefix + (isLeft ? "│   " : "    "), false));

        return sb.toString();
    }

    // Recorrido Preorden (Raíz, Izquierda, Derecha)
    public String preOrderTraversal(ExpressionNode node) {
        if (node == null) {
            return "";
        }
        return node.value + " " + preOrderTraversal(node.left) + preOrderTraversal(node.right);
    }

    // Recorrido Inorden (Izquierda, Raíz, Derecha)
    public String inOrderTraversal(ExpressionNode node) {
        if (node == null) {
            return "";
        }
        return inOrderTraversal(node.left) + node.value + " " + inOrderTraversal(node.right);
    }

    // Recorrido Postorden (Izquierda, Derecha, Raíz)
    public String postOrderTraversal(ExpressionNode node) {
        if (node == null) {
            return "";
        }
        return postOrderTraversal(node.left) + postOrderTraversal(node.right) + node.value + " ";
    }
}


