package cn.edu.bistu.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Stack;

import cn.edu.bistu.calculator.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private LinkedList<Character> expr = new LinkedList<>(); // 存储表达式
    final private Stack<BigDecimal> numbers = new Stack<>();
    final private Stack<Character> operators = new Stack<>();

    private boolean isResult = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 使用view binding简化代码量，将活动的布局文件activity_main.xml绑定到binding对象上
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());//将布局文件显示在activity中
        initClickListener();// 注册listener
        expr.add('0');// 表达式一开始只显示0
        drawExpr();// 显示表达式
    }

    // 绑定ClickListener为onClick函数
    private void initClickListener() {
        binding.textClear.setOnClickListener(this::onClick);
        binding.textBack.setOnClickListener(this::onClick);
        binding.textDivide.setOnClickListener(this::onClick);
        binding.textMultiply.setOnClickListener(this::onClick);
        binding.textMinus.setOnClickListener(this::onClick);
        binding.textAdd.setOnClickListener(this::onClick);
        binding.textEqual.setOnClickListener(this::onClick);
        binding.textMod.setOnClickListener(this::onClick);
        binding.textDot.setOnClickListener(this::onClick);
        binding.textSqrt.setOnClickListener(this::onClick);
        binding.textLeftBracket.setOnClickListener(this::onClick);
        binding.textRightBracket.setOnClickListener(this::onClick);
        binding.text0.setOnClickListener(this::onClick);
        binding.text1.setOnClickListener(this::onClick);
        binding.text2.setOnClickListener(this::onClick);
        binding.text3.setOnClickListener(this::onClick);
        binding.text4.setOnClickListener(this::onClick);
        binding.text5.setOnClickListener(this::onClick);
        binding.text6.setOnClickListener(this::onClick);
        binding.text7.setOnClickListener(this::onClick);
        binding.text8.setOnClickListener(this::onClick);
        binding.text9.setOnClickListener(this::onClick);
    }


    private void onClick(View view) {
        boolean shouldAppend = false;

        if (binding.textClear.equals(view)) { // 清空
            expr.clear();
            expr.add('0');
            if (binding.textClear.getText().equals("AC")) {
                binding.historyText.setText("");
            }

        } else if (binding.textBack.equals(view)) { // 退格
            onBackClick();

        } else if (binding.textDot.equals(view)) { // 小数点
            shouldAppend = onDotClick();

        } else if (binding.textAdd.equals(view) || binding.textMinus.equals(view) // 运算符
                || binding.textMultiply.equals(view) || binding.textDivide.equals(view)
                || binding.textMod.equals(view)) {
            shouldAppend = onOperatorClick((TextView) view);

        } else if (binding.textEqual.equals(view)) { // 求值
            onEqualClick();

        } else // 数字
        {
            if (isResult)  // 如果在计算出结果后又按按下数字，则直接替换（把结果覆盖掉）
            {
                expr.clear();
                expr.add('0');
            }

            shouldAppend = true;
        }

        if (shouldAppend) {
            isResult = false;
            CharSequence input = ((TextView) view).getText();
            if (isCleared() && !".".contentEquals(input)) {
                expr.set(0, input.charAt(0));
            } else {
                expr.add(input.charAt(0));
            }
        }

        drawExpr();
    }

    private void preprocessExpr() {
        if (expr.get(0) == '-') { // 表达式第一个'-'号为负号
            expr.set(0, '@');
        }

        for (int i = 0; i < expr.size() - 1; i++) {
            char c = expr.get(i), next = expr.get(i + 1);

            if (c == '√' && next == '-') {
                throw new ArithmeticException("对负数开根号");
            }

            if (Character.isDigit(c) && next == '√') { // 将2√2这种形式转化为2*√2的形式
                expr.add(i + 1, '×');
            }

            if ((Character.isDigit(c) || c == '.') && next == '(') { // 括号不能直接跟在数字后面
                throw new ArithmeticException("表达式错误");
            }

            if (c == '(' && !Character.isDigit(next) && next != '-' && next != '√' && next != '(') { //左括号后面只能是负号、左括号、根号或者数字
                throw new ArithmeticException("表达式错误");
            }

            if ((c == '(' && next == '-')) { // 标记哪些'-'号是负号
                expr.set(i + 1, '@');
            }
        }
    }

    private String performExpr() {
        numbers.clear();
        operators.clear();
        preprocessExpr();

        StringBuilder number = new StringBuilder();
        for (Character c : expr) {
            if (c == ',')// 忽略逗号
                continue;

            if (Character.isDigit(c) || c == '.') { // 如果是数字
                number.append(c);
            } else { // 运算符
                if (number.length() != 0) { // 保存数字
                    numbers.push(new BigDecimal(number.toString()));
                    number = new StringBuilder();
                }

                Log.d("Main", "c:" + c);
                Log.d("Main", "Before:\nnumbers: " + numbers + "\nops:" + operators);
                if (c != ')') {
                    if (operators.isEmpty() || f(operators.peek()) < g(c)) { // 比栈顶优先级高，入栈
                        operators.push(c);
                    } else {
                        while (!operators.isEmpty() && f(operators.peek()) > g(c)) // 弹出到比栈底优先级低为止
                            calc(operators.pop());
                        operators.push(c);
                    }
                } else {
                    while (operators.peek() != '(' && !operators.isEmpty()) // 弹出到匹配的括号处
                        calc(operators.pop());
                    if (operators.isEmpty())  // 括号不匹配
                        throw new ArithmeticException("括号不匹配");
                    operators.pop(); // 弹出左括号
                }
                Log.d("Main", "After:\nnumbers: " + numbers + "\nops:" + operators);
            }
        }

        if (number.length() != 0)
            numbers.push(new BigDecimal(number.toString()));

        while (!operators.isEmpty()) // 计算最后一步
            if (operators.peek() == '(') // 括号不匹配
                throw new ArithmeticException("括号不匹配");
            else
                calc(operators.pop());


        if (numbers.size() != 1)
            throw new ArithmeticException("错误");

        return numbers.pop().toString();
    }

    private void calc(char c) {
        if (c == '√') {
            BigDecimal a = numbers.pop();
            numbers.push(sqrt(a, 16));// 牛顿迭代法求平方根 精度到小数点后16位
        } else if (c == '@') { // 负号
            BigDecimal a = numbers.pop();
            numbers.push(a.negate());
        } else {
            BigDecimal b = numbers.pop(), a = numbers.pop();
            switch (c) {
                case '+':
                    numbers.push(a.add(b));
                    break;
                case '-':
                    numbers.push(a.subtract(b));
                    break;
                case '×':
                    numbers.push(a.multiply(b));
                    break;
                case '%':
                    numbers.push(a.remainder(b));
                    break;
                case '÷':
                    numbers.push(a.divide(b, 16, RoundingMode.HALF_UP));
            }
        }
    }

    private void onEqualClick() {
        if (isCleared() || isResult)
            return;
        LinkedList<Character> backup = new LinkedList<>(expr);
        try {
            String result = formatResult(performExpr());
            String history = binding.resultText.getText() + "=" + result + "\n";
            binding.historyText.append(history);

            expr.clear();
            for (Character c : result.toCharArray()) {
                expr.add(c);
            }
            isResult = true;
        } catch (Exception e) {
            String msg = e.getMessage();
            Toast.makeText(MainActivity.this, msg == null ? "出错啦" : msg, Toast.LENGTH_SHORT).show();
            expr = backup;
        }
    }

    private void onBackClick() {
        if (!isCleared())
            expr.removeLast();
        if (expr.isEmpty())
            expr.add('0');
    }

    private boolean onDotClick() { // 防止重复添加小数点
        if (expr.isEmpty()) {
            return false;
        }
        return binding.resultText.getText().toString().matches(".*[0-9]$");
    }

    private boolean onOperatorClick(TextView view) { // 防止重复添加运算符
        // 没有输入任何字符的时候只能输入'-'号
        if (isCleared() && !"-".contentEquals(view.getText())) {
            return false;
        }
        // 最后一个符号是小数点则不能添加运算符
        if (expr.getLast() == '.')
            return false;
        // 最后一个字符是运算符的话就直接替换掉，例如1+ -按%号-> 1%
        if (binding.resultText.getText().toString().matches(".*[-+×÷%]")) {
            if (expr.size() > 1)
                expr.removeLast();
            else
                return false;
        }

        return true;
    }

    @SuppressLint("SetTextI18n")
    private void drawExpr() {
        if (!isCleared()) { // 如果当前表达式不为空则，只清除表达式
            binding.textClear.setText("C");
        } else { // 否则清除历史记录
            binding.textClear.setText("AC");
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (Character c : expr) { // 遍历表达式列表
            stringBuilder.append(c); // 将字符追加到字符串构建器中
        }
        binding.resultText.setText(stringBuilder); // 在UI上显示表达式文本
        binding.scrollView.post(() -> binding.scrollView.fullScroll(View.FOCUS_DOWN)); // 滚动到底部
    }

    private boolean isCleared() {
        return (expr.size() == 1 && expr.get(0) == '0') || expr.isEmpty();
    }

    private String formatResult(String resultString) {
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
        numberFormat.setMaximumFractionDigits(16);
        return numberFormat.format(new BigDecimal(resultString));
    }

    /* 优先级函数
     *   + - × / % @ √ (  )
     * f 3 3 5 5 5 7 8 1  10
     * g 2 2 4 4 4 6 9 10 1
     */

    private int f(Character c) {
        switch (c) {
            case '+':
            case '-':
                return 3;
            case '×':
            case '÷':
            case '%':
                return 5;
            case '@':
                return 7;
            case '√':
                return 8;
            case '(':
                return 1;
            default:
                return 10;
        }
    }

    private int g(Character c) {
        switch (c) {
            case '+':
            case '-':
                return 2;
            case '×':
            case '÷':
            case '%':
                return 4;
            case '@':
                return 6;
            case '√':
                return 9;
            case '(':
                return 10;
            default:
                return 1;
        }
    }

    public static BigDecimal sqrt(BigDecimal value, int scale) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ArithmeticException("对负数开根号");
        }

        if (value.equals(BigDecimal.ZERO))
            return BigDecimal.ZERO;

        BigDecimal num2 = BigDecimal.valueOf(2);
        int precision = 100;
        MathContext mc = new MathContext(precision, RoundingMode.HALF_UP);
        BigDecimal deviation = value;
        int cnt = 0;
        while (cnt < precision) {
            deviation = (deviation.add(value.divide(deviation, mc))).divide(num2, mc);
            cnt++;
        }
        deviation = deviation.setScale(scale, RoundingMode.HALF_UP);
        return deviation;
    }
}