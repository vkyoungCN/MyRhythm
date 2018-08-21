package com.vkyoungcn.learningtools.myrhythm.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vkyoungcn.learningtools.myrhythm.R;
import com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor;
import com.vkyoungcn.learningtools.myrhythm.models.CompoundRhythm;

import java.util.ArrayList;

import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.DELETE_MOVE_LAST_SECTION;
import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.MOVE_FINAL_SECTION;
import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.MOVE_LAST_SECTION;
import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.MOVE_LAST_UNIT;
import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.MOVE_NEXT_SECTION;
import static com.vkyoungcn.learningtools.myrhythm.customUI.RhythmSingleLineEditor.MOVE_NEXT_UNIT;


/* 提供基本的逻辑，由其编辑、新建两个方向上的子类分别实现各自要求*/
public class RhythmBaseEditFragment extends Fragment implements View.OnClickListener {

    /* 逻辑*/
    int valueOfBeat = 16;
    int valueOfSection = 64;
    int sectionSize = 4;
    int availableValue = 0;
    int span = 1;

    //交互发回Activity进行，简化复杂问题。
    OnGeneralDfgInteraction mListener;


    CompoundRhythm compoundRhythm = new CompoundRhythm();//【新建不需传递cr但会通过rhythmType构建一个新的comRh；
    // 编辑需要传递comRh。无论如何，都需要将comRh提交给editor】
//    int rhythmType;
    ArrayList<Byte> codes = new ArrayList<>();//都需要对comRh的编码序列进行编辑
    ArrayList<ArrayList<Byte>> codesInSections = new ArrayList<>();//都要使用这个进行处理
    //【RhEditor只是负责显示，逻辑部分其实需要由本fg负责】

    int currentSectionIndex = 0;
    int currentUnitIndexInSection = 0;//在act中依靠这两各变量来确定编辑框位置。

//    Rhythm rhythm ;

    /* 自定义控件*/
    RhythmSingleLineEditor rh_editor_ER;

    /* 35个控件，其中33个（非edt的）有点击事件*/
    TextView tv_x0;
    TextView tv_xb1;
    TextView tv_xb2;
    TextView tv_xb3;

    TextView tv_xp;
    TextView tv_xpb1;
    TextView tv_xpb2;

    TextView tv_xl1;
    TextView tv_xl2;
    TextView tv_xl3;

    TextView tv_xm;
    TextView tv_xm1;
    TextView tv_xm2;

    TextView tv_empty;

    ImageView imv_x0;
    ImageView imv_xb1;
    ImageView imv_xb2;
    ImageView imv_xb3;

    ImageView imv_xp;
    ImageView imv_xpb1;
    ImageView imv_xpb2;

    ImageView imv_xl1;
    ImageView imv_xl2;
    ImageView imv_xl3;

    ImageView imv_xm;
    ImageView imv_xm1;
    ImageView imv_xm2;

    EditText edt_xmNum;
    EditText edt_longCurveNum;

    ImageView imv_longCurve;

    TextView tv_longCurve_remove;
    TextView tv_allConfirm;
    TextView tv_addSection;

    TextView tv_lastSection;
    TextView tv_nextSection;
    TextView tv_lastUnit;
    TextView tv_nextUnit;

    /* 新增控件*/
    TextView tv_InfoRhType;
    TextView tv_InfoBV;
    TextView tv_InfoSV;
    TextView tv_InfoCPRV;



    public RhythmBaseEditFragment() {
        // Required empty public constructor
    }

    public static RhythmBaseEditFragment newInstance(CompoundRhythm compoundRhythm) {
        RhythmBaseEditFragment fragment = new RhythmBaseEditFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("RHYTHM",compoundRhythm);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在各子类具体实现，或是要获取传入的cRh，或是根据传入cRh的rType新建一个空的。
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_edit_rhythm, container, false);

        rh_editor_ER = rootView.findViewById(R.id.rh_editor_ER);

        tv_x0 = rootView.findViewById(R.id.tv_x0_ER);
        tv_xb1 = rootView.findViewById(R.id.tv_xb1_ER);
        tv_xb2 = rootView.findViewById(R.id.tv_xb2_ER) ;
        tv_xb3 = rootView.findViewById(R.id.tv_xb3_ER) ;

        tv_xp = rootView.findViewById(R.id.tv_xp_ER) ;
        tv_xpb1 = rootView.findViewById(R.id.tv_xpb1_ER) ;
        tv_xpb2 = rootView.findViewById(R.id.tv_xpb2_ER) ;

        tv_xl1 = rootView.findViewById(R.id.tv_xl1_ER) ;
        tv_xl2 = rootView.findViewById(R.id.tv_xl2_ER) ;
        tv_xl3 = rootView.findViewById(R.id.tv_xl3_ER) ;

        tv_xm = rootView.findViewById(R.id.tv_xm_ER) ;
        tv_xm1 = rootView.findViewById(R.id.tv_xm1_ER) ;
        tv_xm2 = rootView.findViewById(R.id.tv_xm2_ER) ;

        tv_empty = rootView.findViewById(R.id.tv_empty_ER);

        imv_x0 = rootView.findViewById(R.id.imv_x0_ER);
        imv_xb1 = rootView.findViewById(R.id.imv_xb1_ER) ;
        imv_xb2 = rootView.findViewById(R.id.imv_xb2_ER) ;
        imv_xb3 = rootView.findViewById(R.id.imv_xb3_ER) ;

        imv_xp = rootView.findViewById(R.id.imv_xp_ER) ;
        imv_xpb1 = rootView.findViewById(R.id.imv_xpb1_ER) ;
        imv_xpb2 = rootView.findViewById(R.id.imv_xpb2_ER) ;

        imv_xl1 = rootView.findViewById(R.id.imv_xl1_ER) ;
        imv_xl2 = rootView.findViewById(R.id.imv_xl2_ER) ;
        imv_xl3 = rootView.findViewById(R.id.imv_xl3_ER) ;

        imv_xm = rootView.findViewById(R.id.imv_xm_ER) ;
        imv_xm1 = rootView.findViewById(R.id.imv_xm1_ER) ;
        imv_xm2 = rootView.findViewById(R.id.imv_xm2_ER) ;


        edt_xmNum = rootView.findViewById(R.id.edt_xmNum_ER);
        edt_longCurveNum = rootView.findViewById(R.id.edt_longCurveSpan_ER);

        imv_longCurve = rootView.findViewById(R.id.imv_longCurveEnd_ER);

        tv_longCurve_remove =rootView.findViewById(R.id.tv_longCurveRemove_ER) ;
        tv_allConfirm = rootView.findViewById(R.id.tv_confirmAddRhythm_ER);
        tv_addSection = rootView.findViewById(R.id.tv_addEmptySection_ER);

        tv_lastSection = rootView.findViewById(R.id.tv_lastSection_ER);
        tv_nextSection = rootView.findViewById(R.id.tv_nextSection_ER);
        tv_lastUnit=rootView.findViewById(R.id.tv_lastUnit_ER);
        tv_nextUnit = rootView.findViewById(R.id.tv_nextUnit_ER);

        tv_InfoRhType = rootView.findViewById(R.id.tv_Info_rhType_ER);
        tv_InfoBV = rootView.findViewById(R.id.tv_Info_beatValue_ER);
        tv_InfoSV = rootView.findViewById(R.id.tv_Info_sectionValue_ER);
        tv_InfoCPRV = rootView.findViewById(R.id.tv_Info_currentPlaceRestValue_ER);



        //设监听
        imv_x0.setOnClickListener(this);
        imv_xb1.setOnClickListener(this);
        imv_xb2.setOnClickListener(this);
        imv_xb3.setOnClickListener(this);

        imv_xp.setOnClickListener(this) ;
        imv_xpb1.setOnClickListener(this);
        imv_xpb2.setOnClickListener(this);

        imv_xl1.setOnClickListener(this);
        imv_xl2.setOnClickListener(this);
        imv_xl3.setOnClickListener(this);

        imv_xm.setOnClickListener(this);
        imv_xm1.setOnClickListener(this);
        imv_xm2.setOnClickListener(this);

//        edt_xmNum.setOnClickListener(this);
//        edt_longCurveNum.setOnClickListener(this);

        imv_longCurve.setOnClickListener(this);

        tv_longCurve_remove.setOnClickListener(this);
        tv_addSection.setOnClickListener(this);

        tv_lastSection.setOnClickListener(this);
        tv_nextSection.setOnClickListener(this);
        tv_lastUnit.setOnClickListener(this);
        tv_nextUnit.setOnClickListener(this);

        tv_empty.setOnClickListener(this);

//        tv_allConfirm.setOnClickListener(this);


        //给下方的tv区设置值（对应时值的值的说明区域）
        tv_x0.setText(String.valueOf(valueOfBeat));
        tv_xb1.setText(String.valueOf(valueOfBeat/2));
        tv_xb2.setText(String.valueOf(valueOfBeat/4));
        tv_xb3.setText(String.valueOf(valueOfBeat/8));

        tv_xp.setText(String.valueOf(valueOfBeat+valueOfBeat/2));
        tv_xpb1.setText(String.valueOf(valueOfBeat/2+valueOfBeat/4));
        tv_xpb2.setText(String.valueOf(valueOfBeat/4+valueOfBeat/8));

        tv_xl1.setText(String.valueOf(valueOfBeat*2));
        tv_xl2.setText(String.valueOf(valueOfBeat*3));
        tv_xl3.setText(String.valueOf(valueOfBeat*4));

        tv_xm.setText(String.valueOf(valueOfBeat));
        tv_xm1.setText(String.valueOf(valueOfBeat/2));
        tv_xm2.setText(String.valueOf(valueOfBeat/4));

        tv_InfoRhType.setText(String.format(getContext().getResources().getString(R.string.plh_rh_type),compoundRhythm.getRhythmType()));
        tv_InfoBV.setText(String.format(getContext().getResources().getString(R.string.plh_beat_value),valueOfBeat));
        //onCV在onC之后(在实现类中，onC之后就已经初始化了cRh和编码因而可以设置下列值)
        checkCodeValue(codesInSections.get(0).get(0));
        tv_InfoSV.setText(String.format(getContext().getResources().getString(R.string.plh_section_value),valueOfSection));
        tv_InfoCPRV.setText(String.format(getContext().getResources().getString(R.string.plh_currentPlaceRest_value),availableValue));


//        rh_editor_ER.setRhythm(compoundRhythm);rh编辑器的设置由实现类负责

        return rootView;

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imv_x0_ER :
                changeCode((byte)valueOfBeat);
                break;
            case R.id.imv_xb1_ER :
                changeCode((byte)(valueOfBeat/2));
                break;
            case R.id.imv_xb2_ER :
                changeCode((byte)(valueOfBeat/4));
                break;
            case R.id.imv_xb3_ER:
                changeCode((byte)(valueOfBeat/8));
                break;
            case R.id.imv_xp_ER :
                changeCode((byte)(valueOfBeat+valueOfBeat/2));
                break;
            case R.id.imv_xpb1_ER :
                changeCode((byte)(valueOfBeat/2+valueOfBeat/4));
                break;
            case R.id.imv_xpb2_ER :
                changeCode((byte)(valueOfBeat/4+valueOfBeat/8));
                break;
            case R.id.imv_xl1_ER :
                changeCode((byte)(valueOfBeat*2));
                break;
            case R.id.imv_xl2_ER :
                changeCode((byte)(valueOfBeat*3));
                break;
            case R.id.imv_xl3_ER :
                changeCode((byte)(valueOfBeat*3));
                break;
            case R.id.imv_xm1_ER :
                int fraction = Integer.parseInt(edt_xmNum.getText().toString());
                changeCodeToMultiDivided(8,fraction);
                break;
            case R.id.imv_xm2_ER :
                int fraction_2 = Integer.parseInt(edt_xmNum.getText().toString());
                changeCodeToMultiDivided(9,fraction_2);
                break;
            case R.id.imv_xm_ER :
                int fraction_3 = Integer.parseInt(edt_xmNum.getText().toString());
                changeCodeToMultiDivided(7,fraction_3);
                break;
            case R.id.tv_empty_ER :
                changeToEmpty();
                break;
            case R.id.imv_longCurveEnd_ER:
                int spanNum = Integer.parseInt(edt_longCurveNum.getText().toString());
                if(spanNum>7||spanNum<2){
                    //不合理的跨度
                    Toast.makeText(getContext(), "连音跨度不合理，请检查输入", Toast.LENGTH_SHORT).show();
                    break;
                }
                insertCurveEndAfterCurrent(spanNum);
                break;
            case R.id.tv_longCurveRemove_ER:

                int returnNum = checkAndRemoveLongCurve();
                if(returnNum<0){
                    Toast.makeText(getContext(), "不在连音弧覆盖的范围，没有删除的目标", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.tv_lastSection_ER:
                moveBox(MOVE_LAST_SECTION);
                break;
            case R.id.tv_lastUnit_ER:
                moveBox(MOVE_LAST_UNIT);

                break;
            case R.id.tv_nextSection_ER:
                moveBox(MOVE_NEXT_SECTION);
                break;
            case R.id.tv_nextUnit_ER:
                moveBox(MOVE_NEXT_UNIT);
                break;

           /* case R.id.tv_confirmAddRhythm_ER:
                //子类行为不同
                // 新建：直接前往下一页
                // 编辑，返回上一页（可能还需要上一页是startAFResult的调用方式）
                【由子类负责以匿名接口方式实现】

                break;*/
            case R.id.tv_addEmptySection_ER:
                //在最后添加一个新的小节，编辑框移动到新小节第一位置
                ArrayList<Byte> newSection = new ArrayList<>();
                for (int i=0;i<sectionSize;i++) {
                    newSection.add((byte)-valueOfBeat);//填入负值（显示为空拍0）
                }
                codesInSections.add(newSection);
                moveBox(MOVE_FINAL_SECTION);

                break;

        }
    }

    void changeCode(Byte newCode){
        //【逻辑修改】①要先确定当前框框套住的音符（原值）及原值+其后紧邻的所有空值的总值（可用总值）
        //新值=原值：直接改变该位置上的编码
        //新输入的值<原值，则“拆分”(实际是：删除编码*1，新增编码*2，其中后一音符要与原音符的字符一致)
        //新值>原值，但小于等于总可用值，按所占用的空间生成若干个音符（如果与整拍值不对等，则还要拆分）
        //新值>总可用值，提示先删除。

        byte currentCode = codesInSections.get(currentSectionIndex).get(currentUnitIndexInSection);
//        int valueOfCodeInsideBox =checkCodeValue(currentCode);
        int newValue = checkCodeValue(newCode);//原则上，newCode一定是大于零的。
        availableSpanAndValue();//此方法会自动更新availableValue和span

        if(newValue>availableValue){
            //所需空间比总可用都大，不改变编码
            Toast.makeText(getContext(), "剩余空时值不足，请考虑删除后续临近的既有音符", Toast.LENGTH_SHORT).show();
        }else {
            //可以改变编码
            changeCodeAndNext(newCode,currentSectionIndex,currentUnitIndexInSection,false,0);

            //通知到UI改变
            rh_editor_ER.codeChangedReDraw();

        }
    }


    /* 改变当前位置上的编码，根据新旧时值的不同，产生多种不同的处理情况；可能涉及对后续空拍位置的处理
    * 需要提前判断好后面有多少空余可用值，只要是调用到本方法的情形需要都是在可用可改动的时值总值内的*/
    void changeCodeAndNext(byte toCode,int sectionIndex, int unitIndex,boolean isRecursive,int recursiveTime){
        //一般：需要改成的编码和该位置上编码的情况（仅考虑该位置上的情况）
        byte oldCode = codesInSections.get(sectionIndex).get(unitIndex);
        int toValue = checkCodeValue(toCode);
        int oldValue = checkCodeValue(oldCode);
        //递归标记置真时，一般会产生连音、延音，需要处理。【只在新值小于或等于旧值的情况下，后方才会添加连音结束标记，
        // 而大于时仍然会继续递归，若添加连音结束符会产生错误的多层连音（注：多层连音本身在记谱中并不错误，但此递归中的是另外情形。）】
        //首次调用时，应当手动传入值为0的递归次数

        //如果当前的改变位于旧有连音线内且递归产生新连音线【上递归必然大于一个位置的时值，而如果递归则后面必有空拍或-，理论上这种情形下没有旧连音线】

        int tempCursor = unitIndex;

        if(toValue==oldValue){
            //等时值，直接替换
            codesInSections.get(sectionIndex).set(unitIndex, toCode);
            if(isRecursive){
                //递归调用要在后面添加连音线
                //（但是如果连音的开头一个音符是完整的，则似乎要将中间的完整音符改为-，但逻辑较复杂且并不是很确定乐谱记法暂略，
                // 毕竟不影响谱子的视唱。）
                codesInSections.get(sectionIndex).add(unitIndex+1,(byte)(111+recursiveTime));
            }

        }else if(toValue<oldValue){
            //新值小于旧值
            //又分两种情况（旧值如果是大附点（唯一大于基本节拍的允许编码）则改成一个）
            // 【但是好像没区别，都是改一个，然后剩余部分生成新的（只是大附点下，后续的音符会呈现为至少两半的
            // 但是可以另行手动合并，而不必直接自动合并之。也不应自动合并）】

            //当前位置改变
            codesInSections.get(sectionIndex).set(unitIndex, toCode);
            if(isRecursive){
                tempCursor++;
                //递归调用要在后面添加连音线
                codesInSections.get(sectionIndex).add(tempCursor,(byte)(111+recursiveTime));
            }
            //根据旧值是空拍还是实拍填充不同的新编码（实际为原旧拍剩余）
            if(oldCode>0){
                tempCursor++;
                //后面是实际拍子
                //在后面添加一个剩余时值的拍子
                codesInSections.get(sectionIndex).add(tempCursor,(byte)(oldValue-toValue));
                //指定索引+1位置上（即cUIIS+2）开始的元素都会右移。（添加到指定的索引位置上，即cUIIS+1）

            }else {
                tempCursor++;
                //oldCode<0||oldCode==0都填空拍
                //空拍子，在后面添加一个剩余时值的空拍
                codesInSections.get(sectionIndex).add(tempCursor,(byte)-(oldValue-toValue));
                //【如果进入过递归if分支，则在此临时游标实际=+2】
            }
        }else {
            //新大于旧
            //【原则上，位置上原来是什么时值，新值只能小于或等于原值。即使后方可用总空间大，但由多个小空拍组成也只能拆成“按萝卜坑填入”形式，
            // 若要安排一个更整体的音符，只能先删除后方细分音符（即合并成空拍）】

            if(oldValue==valueOfBeat){
                //旧拍子占整拍
                codesInSections.get(sectionIndex).set(unitIndex, (byte)valueOfBeat);
                //剩余的待填入部分递归调用
                unitIndex++;
                recursiveTime++;//准备递归，递归次数+1。

                if(unitIndex<codesInSections.get(sectionIndex).size()) {
                    changeCodeAndNext((byte) (toCode - valueOfBeat), sectionIndex, unitIndex,true,recursiveTime);
                }else {
                    //跨节
                    unitIndex=0;
                    sectionIndex++;
                    changeCodeAndNext((byte) (toCode - valueOfBeat), sectionIndex, unitIndex,true,recursiveTime);
                }
            }else if(oldValue == valueOfBeat+valueOfBeat/2){
                //旧拍子是大附点
                // 原位置改为整拍
                codesInSections.get(sectionIndex).set(unitIndex, (byte)valueOfBeat);

                //后半按旧值是空拍还是实拍填充不同新编码（实际为新拍子的一部分的后一部分）
                if(oldCode>0){
                    tempCursor++;
                    //后面是实际拍子
                    //在后面添加一个剩余时值(附点部分)的拍子
                    codesInSections.get(sectionIndex).add(tempCursor,(byte)(valueOfBeat/2));
                    //指定索引+1位置上（即cUIIS+2）开始的元素都会右移。（添加到指定的索引位置上，即cUIIS+1）

                }else {
                    tempCursor++;
                    //oldCode<0||oldCode==0都填空拍
                    //空拍子，在后面添加一个剩余时值的空拍
                    codesInSections.get(sectionIndex).add(tempCursor,(byte)-(valueOfBeat/2));
                    //【如果进入过递归if分支，则在此临时游标实际=+2】
                }

                //剩余的新时值，递归处理
                tempCursor++;
                recursiveTime++;//准备递归，递归次数+1。
                if(tempCursor<codesInSections.get(sectionIndex).size()) {
                    changeCodeAndNext((byte) (toCode - oldValue), sectionIndex, tempCursor,true,recursiveTime);
                }else {
                    //跨节
                    tempCursor=0;
                    sectionIndex++;
                    changeCodeAndNext((byte) (toCode - oldValue), sectionIndex, tempCursor,true,recursiveTime);
                }

            }else {
                //旧符一定小于一整拍了
                // 原位置改为可用时值（根据旧值的大小）
                if(oldCode>0){
                    //后面是实际拍子
                    //占据该时值的拍子（相当于不动）
                }else {
                    //空拍子，以和原码相反（等于原码的值）的编码填充
                    codesInSections.get(sectionIndex).set(tempCursor,(byte)oldValue);
                }
                //大于旧值时还没结束，不添加连音弧线。

                //剩余时值到下一个拍子处理
                //剩余的新时值，递归处理
                tempCursor++;
                recursiveTime++;//准备递归，递归次数+1。
                if(tempCursor<codesInSections.get(sectionIndex).size()) {
                    changeCodeAndNext((byte) (toCode - oldValue), sectionIndex, tempCursor,true,recursiveTime);
                }else {
                    //跨节
                    tempCursor=0;
                    sectionIndex++;
                    changeCodeAndNext((byte) (toCode - oldValue), sectionIndex, tempCursor,true,recursiveTime);
                }
            }
        }
    }

    void availableSpanAndValue(){
        //span至少要包括自己在内的（也即至少=1）
        //availableValue从0初始为首位的值
        availableValue+=checkCodeValue(codesInSections.get(currentSectionIndex).get(currentUnitIndexInSection));

        for(int i=currentSectionIndex; i<codesInSections.size();i++){
            ArrayList<Byte> codeInsideSection = codesInSections.get(i);
            if(i==currentSectionIndex) {
                for (int j=currentUnitIndexInSection+1;j<codeInsideSection.size();j++){
                    //从第二个开始计算的
                    byte currentCode = codeInsideSection.get(j);
                    if(currentCode>112){
                        //延音弧线结束标记，不作数
                        continue;//【这里待增加删线逻辑！！！】
                    }else if(currentCode>0){
                        //此时必然小于112，包括：有时值的音符、均分多连音两类，都是“不再可用”，要停止
                        return ;
                    }else if(currentCode==0) {
                        //空拍、延长符，都是可用的
                        span++;
                        availableValue+=valueOfBeat;
                    }else {
                        //只剩小于0
                        span++;
                        availableValue-=currentCode;

                    }
                }
            }else {
                for (Byte code : codeInsideSection) {
                    if(code>112){
                        //延音弧线结束标记，不作数
                        continue;
                    }else if(code>0){
                        //此时必然小于112，包括：有时值的音符、均分多连音两类，都是“不再可用”，要停止
                        return;
                    }else if(code==0) {
                        //空拍、延长符，都是可用的
                        span++;
                        availableValue+=valueOfBeat;
                    }else {
                        //只剩小于0
                        span++;
                        availableValue-=code;
                    }
                }
            }
        }
    }

    int checkCodeValue(byte code) {
        if (code > 112) {
            //上弧连音专用符号，不记时值
            return 0;//但是由于实际上不会选中结束符，因而这种状态是错误的
        } else if (code > 77 || code == 0) {
            //时值计算
            return valueOfBeat;
        } else if (code > 0) {
            //时值计算
            return code;
        } else {//b<0
            //时值计算：空拍带时值，时值绝对值与普通音符相同
            return -code;
        }
    }
    void changeCodeToMultiDivided(int ten, int fraction){
        //确定剩余的可用时值值
        ArrayList<Byte> currentSectionCodes = codesInSections.get(currentSectionIndex);
        int emptyValueInSection = 0;
        for(int i=currentUnitIndexInSection;i<currentSectionCodes.size();i++){
            //只计算本位置后尚余多少时值的空拍
            byte code =currentSectionCodes.get(i);
            if(code< 0 ){
                emptyValueInSection-=code;
            }
        }

        //判断新编码的时值是否符号条件
        if(emptyValueInSection<valueOfBeat){//均分多连音占据一个标准节拍的时值
            Toast.makeText(getContext(), "小节内剩余空时值不足，请考虑删除其他已有音符", Toast.LENGTH_SHORT).show();
        }else {
            byte newCode = (byte) (ten*10+fraction);
            currentSectionCodes.set(currentUnitIndexInSection,newCode);
        }

        //通知到UI改变
        rh_editor_ER.codeChangedReDraw();


    }

    void changeToEmpty(){
        byte b = codesInSections.get(currentSectionIndex).get(currentUnitIndexInSection);
        //变更为同时值大小的
        ArrayList<Byte> codesInThisSection = codesInSections.get(currentSectionIndex);
        codesInThisSection.set(currentUnitIndexInSection,(byte)-b);

        boolean isAllEmpty = true;
        for (byte code:codesInThisSection) {
            if(code>0&&code<112){
                isAllEmpty = false;//只单向设置
                break;
            }
        }
        if(isAllEmpty){
            //本节没有非空的拍子了,应当整节删除
            codesInSections.remove(currentSectionIndex);
            /*
            对索引计数器的调整，由moveB方法负责。
            if(currentSectionIndex>0) {
                currentSectionIndex--;
            }
            currentUnitIndexInSection = 0;*/
            moveBox(DELETE_MOVE_LAST_SECTION);


        }

        //通知到UI改变
        rh_editor_ER.codeChangedReDraw();


    }


    void insertCurveEndAfterCurrent(int span){
        byte code = (byte)(110+span);//这里的跨度从2起，最小是2。
        if((codesInSections.get(currentSectionIndex).size()-1-currentUnitIndexInSection)>0){
            //后面有元素，指定索引插入
            codesInSections.get(currentSectionIndex).add(currentUnitIndexInSection+1,code);
            //据文档：是在指定索引插入元素，该位置原有及后续元素均右移（如果有的话）。

        }else {
            //后面已经没有元素，要附加
            codesInSections.get(currentSectionIndex).add(code);
        }

        //通知到UI改变
        rh_editor_ER.codeChangedReDraw();


    }

    int checkAndRemoveLongCurve(){
        int numForReturn = 0;
        int distanceToCurveEnd = 0;
        ArrayList<Byte> codesInThisSection = codesInSections.get(currentSectionIndex);
        for(int j=currentSectionIndex;j<codesInSections.size()-1;j++) {
            for (int i = currentUnitIndexInSection; i < codesInThisSection.size() - 1; i++) {
                distanceToCurveEnd++;
                byte b = codesInThisSection.get(i);
                if (b > 111) {
                    //是curveEnd
                    if (distanceToCurveEnd <= (b - 110)) {
                        //在有效跨度内，可以移除
                        codesInThisSection.remove(i);
                        //通知到UI改变
                        rh_editor_ER.codeChangedReDraw();
                    } else {
                        numForReturn = -1;//代表不在弧线覆盖范围内
                    }
                    return numForReturn;
                }
            }
            currentUnitIndexInSection = 0;//本节剩余字符内没有连音弧结束标记，需要跨节寻找，重置节内索引。

        }
        //当所有剩余字符都检索完毕仍没有检索到，则
        numForReturn = -2;
        return numForReturn;

    }

    void moveBox(int moveType){
        int result = rh_editor_ER.moveBox(moveType);

        switch (result){
            case 1:
                currentUnitIndexInSection++;
                break;
            case 11:
                currentUnitIndexInSection =0;
                currentSectionIndex++;
                break;
            case -1:
                currentUnitIndexInSection--;
                break;
            case -11:
                currentSectionIndex--;
                currentUnitIndexInSection =(codesInSections.get(currentSectionIndex).size()-1);
                break;
            case -19:
                currentSectionIndex--;
                currentUnitIndexInSection = 0;
                break;
            case -18:
                currentSectionIndex = 0;
                currentUnitIndexInSection = 0;
                break;
            case 20:
                currentSectionIndex = codesInSections.size()-1;
                currentUnitIndexInSection = 0;
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnGeneralDfgInteraction) {
            mListener = (OnGeneralDfgInteraction) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnGeneralDfgInteraction");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
