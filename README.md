# uiModule
Material Designer ui support API 21 above
![Screenshot](https://github.com/cuber5566/uiModule/blob/master/gif/ripple_shadow.gif)


###SquareLinearLayout, SquareRelativeLayout
```xml
<com.ui.uimodule.SquareLinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:sl_widthWeight="3"
        app:sl_heightWeight="4"/>
```
###RaisedButton

```mxl
<com.ui.uimodule.button.RaisedButton
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:background="@android:color/white"
        android:padding="16dp"
        android:text="RaisedButton"
        android:textColor="@android:color/black"
        
        app:rb_backgroundColor="@android:color/white"
        app:rb_rippleColor="@android:color/black"
        app:rb_enableBackgroundColor="@android:color/white"
        app:rb_enableTextColor="@android:color/darker_gray"
        app:rb_rippleRadius="48dp"
        app:rb_rectRadius="16dp"
        app:rb_elevation="5dp"/>
```

###ShadowHelper
```java
 shadowHelper.onDraw(canvas, rectRadius, elevation, focus);
```

###RippleHelper
```java
rippleHelper.onTouch(event);
rippleHelper.onDraw(canvas, rippleRadius, rippleColor, padding, rectRadius);
```
