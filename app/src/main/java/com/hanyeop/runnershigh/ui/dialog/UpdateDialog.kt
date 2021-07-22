package com.hanyeop.runnershigh.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.hanyeop.runnershigh.R

class UpdateDialog(context : Context,
                   myInterface: UpdateDialogInterface,
                   private val title : String,private val image : Bitmap) : Dialog(context) {

    // 액티비티에서 인터페이스를 받아옴
    private var myCustomDialogInterface: UpdateDialogInterface = myInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_dialog)

        var okButton : Button = findViewById(R.id.okButton)
        var cancelButton : Button = findViewById(R.id.cancelButton)
        var editView : EditText = findViewById(R.id.titleEditView)
        var imageView : ImageView = findViewById(R.id.imageView)

        // 배경 투명하게 바꿔줌
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 제목 불러오기
        editView.setText(title)

        // 이미지 불러오기
        Glide.with(context).load(image).into(imageView)

        okButton.setOnClickListener {
            val content = editView.text.toString()

            // 입력하지 않았을 때
            if ( TextUtils.isEmpty(content)){
                Toast.makeText(context, "수정할 제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }

            // 입력 창이 비어 있지 않을 때
            else{
                // 수정 내용 전달
                myCustomDialogInterface.onOkButtonClicked(content)
                dismiss()
            }
        }

        // 취소 버튼 클릭 시 종료
        cancelButton.setOnClickListener { dismiss()}
    }
}