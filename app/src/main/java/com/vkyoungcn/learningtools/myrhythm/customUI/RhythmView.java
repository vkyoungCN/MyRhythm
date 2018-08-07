package com.vkyoungcn.learningtools.myrhythm.customUI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.vkyoungcn.learningtools.myrhythm.R;

import java.util.ArrayList;

public class RhythmView extends View {
//* 显示节奏序列
//* 有“折行、单行”两种模式；字符及字宽有“大中小三种模式”
//* 无数据时先将字符区绘制rect形式；填充数据后再根据数据生成绘制信息重新绘制。
//*
//* 三种工作模式：“节奏/旋律/有词”
//* 其中有词模式下需要绘制上方连音弧线；旋律模式下需要绘制上下方的加点。
//*

    private static final String TAG = "RhythmView";

    /* 常量*/
    public static final int CODE_SMALL = 2001;
    public static final int CODE_MEDIUM = 2002;
    public static final int CODE_LARGE = 2003;

    public static final int RHYTHM_TYPE_24 = 2004;
    public static final int RHYTHM_TYPE_34 = 2005;
    public static final int RHYTHM_TYPE_44 = 2006;
    public static final int RHYTHM_TYPE_38 = 2007;
    public static final int RHYTHM_TYPE_68 = 2008;




    private Context mContext;

    private ArrayList<Byte> rhythmCodes; //数据源，节奏序列的编码。根据该数据生成各字符单元上的绘制信息。
    private int rhythmType;//节拍类型（如4/4），会影响分节的绘制。【不能直接传递在本程序所用的节奏编码方案下的时值总长，因为3/4和6/8等长但绘制不同】
    private DrawingUnit drawingUnits[];

    private ArrayList<ArrayList<Byte>> codesInSections;//对数据源进行分析处理之后，按小节归纳起来。便于进行按节分行的判断。

    /* 设置参数*/
    //设置参数与数据源一并设置
    private boolean useMelodyMode = false;//如果使用旋律模式，则需要数字替代X且在onD中处理上下加点的绘制。
    private boolean useMultiLine = true;//在某些特殊模式下，需使用单行模式（如在显示某节奏所对应的单条旋律时，计划以可横向滑动的单行模式进行显示，以节省纵向空间。）


    //    private boolean isDataInitBeInterruptedBecauseOfNoSize = false;

    /* 画笔组*/
    private Paint bottomLinePaint;
    private Paint codePaint;
    private Paint grayEmptyPaint;//在无数据时，在字符行绘制背景替代。
    private Paint codeUnitOutLinePaint;//在编辑模式下，修改的位置上绘制浅蓝色方框
//    private Paint textWaitingPaint;

    /* 尺寸组 */
    private float padding;
    private float unitStandardWidth;//24dp。单个普通音符的基准宽度。【按此标准宽度计算各节需占宽度；如果单节占宽超屏幕宽度，则需压缩单节内音符的占宽；
    // 如果下节因为超长而移到下一行，且本行剩余了更多空间，则需要对各音符占宽予以增加（但是字符大小不变）】
//    private float unitSizeMedium;//30
//    private float unitSizeLarge;//36

    private float unitWidth = unitStandardWidth;//最终选定的单位尺寸【默认最小】
    private float beatGap;//节拍之间、小节之间需要有额外间隔（但似乎没有统一规范），暂定12dp。
    //注意，一个节拍内的音符之间没有额外间隔。
    private float dotExtra = unitWidth /2;//当绘制附点时（按照字串“X·”绘制，字宽要增大，额外安排一些空间）
    //但是好像无法手动控制X和点之间的间距，因而本参数仅用于让后续字符自然。//暂定半宽

    private float lineGap;//不同行之间的间隔。暂定12dp；如果有文字行则需额外安排文字空间。
    private float additionalHeight;//用于上下加点绘制的保留区域，暂定6dp
    private float curveOrLinesHeight;//用于绘制上方连音线或下方下划线的空间（上下各一份），暂定8dp

    //下划线绘制为2dp(或1dp)每条
    private float textSizeSmall;//14sp
    private float textSizeMedium;//16sp
    private float textSizeLarge;//24sp


    private float textSize;//【考虑让文字尺寸后期改用和section宽度一致或稍小的直接数据.已尝试不可用】
    private float textBaseLineBottomGap;

    int lines = 1;//控件需要按几行显示，根据当前屏幕下控件最大允许宽度和控件字符数（需要的宽度）计算得到。

    private int sizeChangedHeight = 0;//是控件onSizeChanged后获得的尺寸之高度，也是传给onDraw进行线段绘制的canvas-Y坐标(单行时)
    private int sizeChangedWidth = 0;//未获取数据前设置为0


    /* 色彩组 */
    private int generalColor_Gray;


    //用于描述各字符对应的下划线的一个内部类
    public class DrawingUnit {
        //在横向上，各字符基本是等宽的；
        // 但是当位于节拍或小节末尾时，右侧会附加上额外的空间
        // 这些额外的空间会影响后续字符的横向位置，因而必须记录到所有受影响的字符中；
        //（另外，小节之间的小节线的绘制信息不记录在DU中，而是由onD方法现场计算绘制。但位于小节末尾
        // 的DU中会持有一个节末标记变量）


        //用于描述各音符下划线绘制信息的类，用在DrawingUnit中
        private class BottomLine {
            float startX;
            float startY;
            float toX;
            float toY;

            public BottomLine(float startX, float startY, float toX, float toY) {
                this.startX = startX;
                this.startY = startY;
                this.toX = toX;
                this.toY = toY;
            }
        }


        private String code = "X";//默认是X，当作为旋律绘制时绘制具体音高的数值。
        private boolean isLastCodeInSection = false;//如果是小节内最后一个音符，需要记录一下，以便在遍历绘制时在后面绘制一条竖线（小节线）
        //拍子之间、小节之间有额外间隔，由设置方法计算出位置后直接存储给相应字段，本类不需持有相应位置信息。
        //最前端的一条小节线由绘制方法默认绘制，不需记录。

//        private byte additionalSpotType = 0;//上下加点类型，默认0（无加点）；下加负值、上加正值。原则上不超正负3。
//        private byte bottomLineAmount = 0;//并不是所有音符都有下划线。


        private float codeCenterX;//用于字符绘制（字符底边中点）
        private float codeBaseY;//字符底边【待？基线还是底边？】

        private ArrayList<BottomLine> bottomLines = new ArrayList<>();
        private RectF[] additionalPoints = new RectF[]{};//上下加点

        /* 作为一个绘制单位，其整体的左端起始位置
        * 用于使后续单位建立自己的位置*/
        float left;
        float right;
        float top;
        float bottom;
        //一个字符的空间方案暂定如下：标志尺寸ss,字符区域占宽=ss（字体尺寸本身不足的留空即可），
        // 字符占高=展宽；字符上方预留半ss的顶弧线高度，其中保留一小层的高度作为上加点区域；
        //字符下方半ss空间是下划线区域，下划线下方保留一小层高度作为下加点区域。（小层高度待定，暂定5~8dp）
        //非首尾拍字符之间是没有间隔的，以便令下划线相接。

//        private float firstBottomLineFromX;//其实所有下划线的X一致
//        private float firstBottomLineFromY;//第2、3线手动加上间隔像素值（暂定间隔4像素）
//        private float firstBottomLineToX;
//        private float firstBottomLineToY;

        //连音线的绘制，将由RhV直接提供方法。程序根据词序缺少位置，指定Rhv在哪些（起止）位置上绘制连音线


        public DrawingUnit() {
        }

        public DrawingUnit(String code, boolean isLastCodeInSection, float codeCenterX, float codeBaseY, ArrayList<BottomLine> bottomLines, RectF[] additionalPoints, float left, float right, float top, float bottom) {
            this.code = code;
            this.isLastCodeInSection = isLastCodeInSection;
            this.codeCenterX = codeCenterX;
            this.codeBaseY = codeBaseY;
            this.bottomLines = bottomLines;
            this.additionalPoints = additionalPoints;
            this.left = left;
            this.right = right;
            this.top = top;
            this.bottom = bottom;
        }


        /*public void setBottomLineAmount(byte bottomLineAmount) {
            if(bottomLineAmount>3){
                Toast.makeText(mContext, "音符下划线过多？请检查谱子是否正确。", Toast.LENGTH_SHORT).show();
                return;
            }else if (bottomLineAmount<0){
                Toast.makeText(mContext, "音符下划线数值设置错误。", Toast.LENGTH_SHORT).show();

            }
            this.bottomLineAmount = bottomLineAmount;
        }*/

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            //从编码到code需要转换（即使是音高，也因上下加点而多种不同）
            this.code = code;
        }


/*
        public void setAdditionalSpotType(byte additionalSpotType) {
            if(additionalSpotType>4||additionalSpotType<-4){
                Toast.makeText(mContext, "上下加点异常，请检查输入是否错误。", Toast.LENGTH_SHORT).show();
            }else {
                this.additionalSpotType = additionalSpotType;
                //在不超正负4，但超正负2时，可以设置，但要给出提示。
                if(additionalSpotType>2){
                    Toast.makeText(mContext, "上加点超过2，可能超出可演唱音域。", Toast.LENGTH_SHORT).show();
                }else if(additionalSpotType<-2){
                    Toast.makeText(mContext, "下加点超过2，可能超出可演唱音域。", Toast.LENGTH_SHORT).show();
                }
            }
        }
*/

        public float getCodeCenterX() {
            return codeCenterX;
        }

        public void setCodeCenterX(float codeCenterX) {
            this.codeCenterX = codeCenterX;
        }

        public float getCodeBaseY() {
            return codeBaseY;
        }

        public void setCodeBaseY(float codeBaseY) {
            this.codeBaseY = codeBaseY;
        }


        public boolean isLastCodeInSection() {
            return isLastCodeInSection;
        }

        public void setLastCodeInSection(boolean lastCodeInSection) {
            isLastCodeInSection = lastCodeInSection;
        }

        public ArrayList<BottomLine> getBottomLines() {
            return bottomLines;
        }

        public void setBottomLines(ArrayList<BottomLine> bottomLines) {
            this.bottomLines = bottomLines;
        }

        public RectF[] getAdditionalPoints() {
            return additionalPoints;
        }

        public void setAdditionalPoints(RectF[] additionalPoints) {
            this.additionalPoints = additionalPoints;
        }
    }


    public RhythmView(Context context) {
        super(context);
        mContext = context;
        init(null);
//        this.listener = null;
    }

    public RhythmView(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        mContext = context;
        init(attributeset);
//        this.listener = null;
    }


    public RhythmView(Context context, AttributeSet attributeset, int defStyledAttrs) {
        super(context, attributeset, defStyledAttrs);
        mContext = context;
        init(attributeset);
//        this.listener = null;
    }


    private void init(AttributeSet attributeset) {
        initSize();
        initColor();
        initPaint();
        initViewOptions();
    }


    private void initSize() {
        padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        unitStandardWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
//        unitSizeMedium = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
//        unitSizeLarge = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, getResources().getDisplayMetrics());

        beatGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
        lineGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
        additionalHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics());
        curveOrLinesHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());

//        heightAddition_singleSide = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        textBaseLineBottomGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());


        textSizeSmall =  (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics());
        textSizeMedium =  (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics());
        textSizeLarge =  (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22, getResources().getDisplayMetrics());
    }

    private void initColor(){
        generalColor_Gray = ContextCompat.getColor(mContext, R.color.rhythmView_generalGray);
    }


    private void initPaint() {
        bottomLinePaint = new Paint();
        bottomLinePaint.setColor(generalColor_Gray);
        bottomLinePaint.setStrokeWidth(2);//
        bottomLinePaint.setStyle(android.graphics.Paint.Style.STROKE);


        codePaint = new Paint();
        codePaint.setTextSize(textSize);
//        codePaint.setStrokeWidth(4);
        codePaint.setColor(generalColor_Gray);
        codePaint.setAntiAlias(true);
        codePaint.setTextAlign(Paint.Align.CENTER);

        grayEmptyPaint = new Paint();
        grayEmptyPaint.setStyle(Paint.Style.FILL);
        grayEmptyPaint.setStrokeWidth(4);
        grayEmptyPaint.setAntiAlias(true);
        grayEmptyPaint.setColor(generalColor_Gray);

    }


    private void initViewOptions() {
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    //【学：据说是系统计算好控件的实际尺寸后以本方法通知用户】
    // 【调用顺序：M(多次)-S(单次)-D】。
    @Override
    protected void onSizeChanged(int w, int h, int old_w, int old_h) {
        sizeChangedHeight = h;
        sizeChangedWidth = w;

        if(!rhythmCodes.isEmpty()){
            //如果数据源此时非空，则代表数据源的设置早于onSC，也即数据源设置方法中的绘制信息初始化方法被中止，
            //需要再次再次初始化绘制信息（但是传入isTBOSC标记，只初始绘制信息不进行刷新）
            initDrawingUnits(true);
        }//【这里如是单线程则没有问题，如果是多线程，则可能在initDU返回前就调用了onDraw，则可能无法正确绘制，
        // 那样的话取消传入“禁止刷新”的标记即可。（倒是应该不会下标越界，毕竟是for是有判断的）】


        //万一程序对targetCode的设置（数据初始化）早于控件尺寸的确定（则是无法初始化下划线数据的），
        // 则需要在此重新对下划线数据进行设置
        /*if(isDataInitBeInterruptedBecauseOfNoSize){
            isDataInitBeInterruptedBecauseOfNoSize =false;//表示事件/状况已被消耗掉
//            Log.i(TAG, "onSizeChanged: initBL");
            initDrawingUnits(w,rhythmCodes.size());
        }*/

        super.onSizeChanged(w, h, old_w, old_h);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //合理的尺寸由外部代码及布局文件实现，这里不设计复杂的尺寸交互申请逻辑，而是直接使用结果。
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), View.MeasureSpec.getSize(heightMeasureSpec));

    }


    @Override
    protected void onDraw(Canvas canvas) {
//        Log.i(TAG, "onDraw: characters="+characters.toString());
        if(rhythmCodes.isEmpty()) {
            //此时节奏数据还未设置，只在中间高度绘制一条背景
            float fromX = padding;
            float fromY = sizeChangedHeight/2-20;
            float toX = sizeChangedWidth - padding;
            float toY = sizeChangedHeight/2+20;
            //条带总宽40像素。

            canvas.drawRect(fromX,fromY,toX,toY, grayEmptyPaint);
            return;
        }
        //注意，小节线绘制规则：起端没有小节线，小节线只存在于末尾

        //逐音符绘制
        for (int i = 0; i < drawingUnits.length; i++) {
            DrawingUnit drawingUnit = drawingUnits[i];

            //字符
            canvas.drawText(drawingUnit.getCode(),drawingUnit.getCodeCenterX(),drawingUnit.getCodeBaseY(), codePaint);

            //下划线绘制
            for (DrawingUnit.BottomLine bottomLine : drawingUnit.bottomLines) {
                canvas.drawLine(bottomLine.startX, bottomLine.startY,bottomLine.toX,bottomLine.toY,bottomLinePaint);
            }

            //绘制上下加点（旋律模式下）
            for (RectF point : drawingUnit.additionalPoints){
                canvas.drawOval(point,bottomLinePaint);
            }

/*
            int bottomLineAmount = drawingUnit.getBottomLineAmount();
            if(bottomLineAmount>0){
                //>0时先绘制第一条
                canvas.drawLine(drawingUnit.getFirstBottomLineFromX(), drawingUnit.getFirstBottomLineFromY(), drawingUnit.getFirstBottomLineToX(), drawingUnit.getFirstBottomLineToY(), bottomLinePaint);
            }
            if(bottomLineAmount>1){
                //>1，再绘制第二条(此时>0也满足因而第一条已经绘制，以下同理)
                //【各条线线宽2像素，线间隔2像素。】
                canvas.drawLine(drawingUnit.getFirstBottomLineFromX(), drawingUnit.getFirstBottomLineFromY()+4, drawingUnit.getFirstBottomLineToX(), drawingUnit.getFirstBottomLineToY()+4, bottomLinePaint);
            }
            if(bottomLineAmount>2){
                //>2，再绘制第3条
                canvas.drawLine(drawingUnit.getFirstBottomLineFromX(), drawingUnit.getFirstBottomLineFromY()+8, drawingUnit.getFirstBottomLineToX(), drawingUnit.getFirstBottomLineToY()+8, bottomLinePaint);
            }//最多绘制三条；如果==0则不绘制下划线

*/

            //如果是小节末尾，要绘制尾端小节竖线
            if(drawingUnit.isLastCodeInSection){
                float fromX = drawingUnit.right+beatGap/2;
                float fromY = drawingUnit.top+additionalHeight+curveOrLinesHeight;//【目标是与字符同高，但不知是否如此实现】
                float toX = drawingUnit.right+beatGap/2;//竖线，x不变。
                float toY = drawingUnit.bottom+curveOrLinesHeight;//应与第三条线高度约同【暂定画到底线区最下】）。
                canvas.drawLine(fromX, fromY, toX, toY, bottomLinePaint);
            }

        }

        //在有词模式下绘制上方连音线【待】



//            invalidate();

    }


/*    public String getCurrentString() {
        StringBuilder sbd = new StringBuilder();
        for (Character c :
                characters) {
            sbd.append(c);
        }

        return sbd.toString();
    }*/




    /*
     * 方法由程序调用，动态设置目标字串
     * 设置节拍类型（4/4等）
     * 设置字符大小
     * */
    public void setRhythmViewInfo(ArrayList<Byte> rhythmCodes,byte rhythmType, int codeSize){
        this.rhythmCodes = rhythmCodes;
        this.rhythmType = rhythmType;
        switch (codeSize){
            case CODE_SMALL:
                this.textSize = textSizeSmall;
                break;
            case CODE_MEDIUM:
                this.textSize = textSizeMedium;
                break;
            case CODE_LARGE:
                this.textSize = textSizeLarge;
                break;
        }

        //由于要根据目标字串的字符数量来绘制控件，所以所有需要用到该数量的初始化动作都只能在此后进行
        initData();
    }

    //onSizeC方法中会调用initData，该时点可能尚未设置必要的数据，所以需要判断。??
    //事实上，onS和setRC谁先谁后可能没有定数。两种错误都遇到过。??


    private void initData() {
        //根据初始化了的数据源初始化绘制信息数组
        //本方法在设置数据源的方法内调用，因而必然非空不需判断
        drawingUnits = new DrawingUnit[rhythmCodes.size()];

        //如果这个时候还没有获取到尺寸信息则终止操作
        // 在onSizeChanged()中会对isDataInitBeInterruptedBecauseOfNoSize变量进行判断；
        // 发现终止记录时将再次恢复（直接进入下一级方法）。
        if(sizeChangedWidth == 0){
//            isDataInitBeInterruptedBecauseOfNoSize = true;
            return;
        }
        initDrawingUnits(false);

    }

    private void initDrawingUnits(boolean isTriggerByOnSC) {

        int unitAmount = rhythmCodes.size();

        drawingUnits = new DrawingUnit[unitAmount];
        for(int i=0;i<rhythmCodes.size();i++){//必须得这样彻底初始化，如果只有上一句而不进行for循环初始则崩溃。
            drawingUnits[i] = new DrawingUnit();
        }

        //可用总长（控件宽扣除两侧缩进）
        float availableTotalWidth = sizeChangedWidth - padding*2;

        int valueOfSection = 64;//默认64，4/4。
        int valueOfBeat = 16;//默认一拍16值。
        switch (rhythmType){
            case RHYTHM_TYPE_24:
                valueOfSection = 32;
                //此时beat值==16无需修改
            break;
            case RHYTHM_TYPE_34:
                valueOfSection = 48;
                break;
            case RHYTHM_TYPE_44:
                valueOfSection = 64;
                break;
            case RHYTHM_TYPE_38:
                valueOfSection = 24;
                valueOfBeat = 8;
                break;
            case RHYTHM_TYPE_68:
                valueOfSection = 48;
                valueOfBeat = 8;
                break;

        }

        //判断总长，以便推断是需要居中绘制还是靠左上绘制（并且得出起绘点的Y坐标。）
        int totalValue = 0;
        float requiredTotalLength = 0;
        boolean isFirstSectionInThisLine = true;//是该行首节（首节则当宽度不足以显示时需要压缩，非首节则简单移到下一节（然后同样置真，情况前面累加后再判断））
        codesInSections = new ArrayList<>();//初步初始化
        ArrayList<Byte> codeInSingleSection = new ArrayList<>();//单节内的音符【由于引用的规则，本变量仅能供首节使用，以后需额外再次初始。】
        int sectionsBeforeThisLine = 0;//在本行之前一共出现了多少小节。

        for (byte b:rhythmCodes){
            if(b>77){
                //均分多连音
                requiredTotalLength += (unitWidth /2)*(b%10);
                //时值计算
                totalValue += valueOfBeat;
            }else if(b==16||b==8||b==4||b==2) {
                //是不带附点的音符,占据标准宽度
                requiredTotalLength+= unitWidth;
                //时值计算
                totalValue+=b;
            }else if (b==0){
                //延长音，且不是均分多连音；即
                requiredTotalLength+= unitWidth;
                //时值计算，独占节拍的延长音
                totalValue+=valueOfBeat;
            }else if (b==-2||b==-4||b==-8||b==-16){
                //空拍（不带附点时）也占标准宽
                requiredTotalLength+= unitWidth;
                //时值计算：空拍带时值，时值绝对值与普通音符相同
                totalValue-=b;
            }else if(b==24||b==12||b==6||b==3){
                //带附点，标准宽*1.5
                requiredTotalLength += unitAmount*1.5;
                //时值计算
                totalValue+=b;
            }else if(b==-3||b==-6||b==-12||b==-24){
                //带附点，标准宽*1.5
                requiredTotalLength += unitAmount*1.5;
                //时值计算
                totalValue-=b;
            }
            codeInSingleSection.add(b);//先把这个音节加入（按音阶组织的列表之）本小节。

            //给拍间添加拍间隔
            if(totalValue%valueOfBeat==0){
                //到达一拍末尾
                //另：大附点（基本音符附加附点）的宽度不再额外加入拍间隔（因为很难计算、逻辑不好处理；而且附点本身有一个间隔，绘制效果应该也还可以）
                requiredTotalLength+=beatGap;
            }

            //本节末尾时判断能否在屏幕宽度内一行显示
            if (totalValue%valueOfSection==0){
                //到达小节末尾，将本小节提交到按节组织的编码列表总表
                codesInSections.add(codeInSingleSection);
                codeInSingleSection = new ArrayList<>();//再次初始化，用于下一小节装载。

                if(requiredTotalLength>availableTotalWidth){
                    //无法在一行内绘制完成。如果只有一节则压缩，否则将本节下移
                    int sectionsInsideThisLine = codesInSections.size()-sectionsBeforeThisLine;
                    if(sectionsInsideThisLine==1){
                        //本行只有一节，需要压缩本节内字符的基础宽度
                        float unitWidth = (availableTotalWidth/requiredTotalLength)*unitStandardWidth;//与外部使用的uW变量同名

                    }else {
                        //本行超过一节，本节下移；“本”行之前的所有保留节需要扩展宽度

                        //①从codesInSections.get(codesInSections.size()-2)
                        //到codesInSections.get(codesInSections.size()-sectionsInsideThisLine)
                        //需要扩展宽度【待】

                        //②codesInSections.get(codesInSections.size()-1)本节下移【待】
                        if(再次判断本节在新的一行内能否显示完全)
                        需要将长度判断封装成“按小节判断”；至少要提供一个能够按小节进行判断的方法（否则这里就得再写一遍上方的多分支）
                        或许考虑先对编码进行两遍处理，第一遍先处理成按节组织的嵌套列表形式。
                        //重置



                    }


                }else {

                }

            }

        }
        int beatAmount = totalValue/valueOfBeat;
        requiredTotalLength += beatAmount*beatGap;//节拍间的间隔（包括小节间的间隔）也要加上
        int requiredLines = 1;//默认行数1。

        if(requiredTotalLength - availableTotalWidth >0){
            //超出一行
            requiredLines = (int)(requiredTotalLength/availableTotalWidth)+1;
        }
        float requiredHeight = requiredLines*(unitWidth +additionalHeight*2+curveOrLinesHeight*2);

        float topLineTopY = padding;//起绘位置（最高行顶部的Y坐标）
        if(requiredHeight<sizeChangedHeight){
            //所需高度未超控件可绘制高度，就不能从顶部起绘
            topLineTopY = sizeChangedHeight/2 - requiredHeight/2;

        }



        //根据音符时值及乐谱绘制规则，生成各音符的绘制坐标
        //第一个音符特别处理
        //第一个音符的起始位置必然是靠左端、靠上端，边距之后。
        drawingUnits[0].left = padding;
        drawingUnits[0].top = padding;
        //不论有无上下加点，都需要保留上下额外空间
        //字符绘制区域在纵向上包括上下额外附加的共计4个附加高度区。

        drawingUnits[0].bottom = padding+additionalHeight*2+curveOrLinesHeight*2+ unitWidth;
        //字符的横向空间要根据音符是否有附点（占宽1.5）、是否是均分多连音判定(占宽：连音数量*半宽)。
        //时值小于基本音符的音符，占宽始终是标准宽度
        int currentCode = rhythmCodes.get(0);
        if(currentCode>0&&currentCode<=valueOfBeat){
            //正常宽度的音符
            drawingUnits[0].right = padding+ unitWidth;

            //根据时值绘制下划线
            switch (currentCode){
                case valueOfBeat/2:
                case valueOfBeat/2+valueOfBeat/4:
                    //一条下划线
                    drawingUnits[0].bottomLines.add(new DrawingUnit.BottomLine(drawingUnits[0].left,,drawingUnits[0].right,))
            }



        }else if(currentCode== valueOfBeat+valueOfBeat/2){
            //首位就是大附点
            drawingUnits[0].right = padding+ unitWidth + unitWidth /2;
        }else if(currentCode>=73 && currentCode<= 99 ){
            //均分多连音
            drawingUnits[0].right = padding+(unitWidth /2)*(currentCode%10);
        }else if(currentCode<0){
            //空拍子【空拍子应该没见过基本音符+大附点形式的。暂不处理，如果遇到再修改】
            //那么宽度是基本宽度，符号改成0
            drawingUnits[0].right = padding+ unitWidth;
            drawingUnits[0].code ="0";

        }//首音符不可能是延长音符号“-”




        float currentTotalWidth = 0;//累加长度；
        int totalValueInThisSection = 0;//从本小节首开始计算的时值长度；跨节后重置。
        //计算能否按指定尺寸在一行内容纳所有内容，否则计算出所要占据的行数和何处换行
        for(int i =0; i<unitAmount;i++){
            byte currentCode = rhythmCodes.get(i);



            if(currentCode>0&&currentCode<=valueOfBeat){
                //这个范围内的编码，代表正常普通音符或附点的不足1/4时值的音符，可以正常计算时值
                totalValueInThisSection += currentCode;

                //【还要考虑换行影响啊】待





            }else if(currentCode== valueOfBeat+valueOfBeat/2){
                //大附点

                //其他情形处理【如均分多连音、带时值空拍等】

            }else if(currentCode>=97 && currentCode<=109 ){
                //均分多连音


            }else if(currentCode == 0){
                //延长音符号“-”


            }else if(currentCode<0){
                //空拍子【且有具体时值要计算】


            }

            //判断是否到达本拍末尾（是则将派间隔直接写入本字符的右侧空间内【即由间隔前的字符负责拍间的间隔】）
            //判断是否到达本节结尾，若是，相应变量置true。
            if(totalValueInThisSection%valueOfBeat==0&&totalValueInThisSection!=valueOfSection){
                //拍结尾，不是小节结尾
                drawingUnits
            }


        }
        if((unitAmount*maxSectionWidth+(unitAmount-1)*sectionGapLarge<=availableTotalWidth)){
            //可以容纳，使用大尺寸间隔和既定的大尺寸每节长度、大尺寸字符
            for (int i = 0; i < unitAmount; i++) {
                drawingUnits[i].firstBottomLineFromX =padding +(maxSectionWidth+sectionGapLarge)*i;
                //注意，先确定下方位置再确定上方位置。说明控件是靠下的gravity。
                drawingUnits[i].firstBottomLineFromY = sizeChangedHeight-padding;
                drawingUnits[i].firstBottomLineToY = sizeChangedHeight-padding;
                drawingUnits[i].firstBottomLineToX = drawingUnits[i].firstBottomLineFromX +maxSectionWidth;
//                Log.i(TAG, "initDrawingUnits: fx="+drawingUnits[i].firstBottomLineFromX);
//                Log.i(TAG, "initDrawingUnits: fy="+drawingUnits[i].firstBottomLineFromY);

            }
        }else {
            //使用小尺寸间隔以及动态确定的每节长度【字符大小还需另行确定】
            float totalWidthPureForLines =  availableTotalWidth-((unitAmount-1)*sectionGapSmall);
            finalLineWidth = totalWidthPureForLines/unitAmount;

            for (int i = 0; i < unitAmount; i++) {
                drawingUnits[i].firstBottomLineFromX =padding +(finalLineWidth+sectionGapSmall)*i;
                //注意，先确定下方位置再确定上方位置。说明控件是靠下的gravity。
                drawingUnits[i].firstBottomLineToY = sizeChangedHeight-padding;
                drawingUnits[i].firstBottomLineFromY =sizeChangedHeight-padding;
                drawingUnits[i].firstBottomLineToX = drawingUnits[i].firstBottomLineFromX +finalLineWidth;
            }

            //改文字画笔的字号大小
            float smallerTextSize = (textSize/maxSectionWidth)*finalLineWidth;
            codePaint.setTextSize(smallerTextSize);
            textWaitingPaint.setTextSize(smallerTextSize);
            textErrPaint.setTextSize(smallerTextSize);

        }
//        Log.i(TAG, "initDrawingUnits: invalidate, init="+initText);

        if(!isTriggerByOnSC) {
            //如果是在onSizeChanged方法内被触发，则不应调用刷新方法（因为会自动继续调用onDraw()）
            invalidate();//完成了目标数据、下划线的初始化后刷新控件。
        }
/*
        if(lines ==1) {
            for (int i = 0; i < bottomLinesAmount; i++) {
                drawingUnits[i] = createPath(i, 1,1, finalSectionWidth);
            }
        }else {
            int sectionsMaxAmountPerLine = (int)(viewMaxWidth/ finalSectionWidth);
            for (int i = 0; i < bottomLinesAmount; i++) {
                int currentLine = (i/sectionsMaxAmountPerLine)+1;
                int positionInLine = i%sectionsMaxAmountPerLine;
                drawingUnits[i] = createPath(positionInLine, lines,currentLine, finalSectionWidth);
            }
        }
*/
    }

    /*
     * 有时候VE带有初始数据（比如卡片再次滑回时）
     * */
    public void setInitText(String initText) {
        this.initText = initText;
//        Log.i(TAG, "setInitText: String Received in Ve = "+initText);

        //存入数据数组
        if(initText!=null){
            //先对旧数据清空
            characters.clear();

            //存入数据数组
            char[] chars = initText.toCharArray();
            for (char c: chars){
                characters.push(c);
            }
        }

        invalidate();//刷新显示。
    }
}
