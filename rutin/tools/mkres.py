import os, sys
os.chdir(os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'app'))
F = {}
F['res/values/styles.xml'] = '''<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="bg">#0F0F17</color>
    <style name="AppTheme" parent="@android:style/Theme.Material.NoActionBar">
        <item name="android:windowBackground">@color/bg</item>
        <item name="android:colorAccent">#FF7A45</item>
        <item name="android:colorPrimary">#0F0F17</item>
        <item name="android:colorPrimaryDark">#0F0F17</item>
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">#0F0F17</item>
    </style>
</resources>
'''
F['res/values/strings.xml'] = '''<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Rutin</string>
</resources>
'''
F['res/mipmap-anydpi-v26/ic_launcher.xml'] = '''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_bg" />
    <foreground android:drawable="@drawable/ic_launcher_fg" />
</adaptive-icon>
'''
F['res/drawable/ic_launcher_bg.xml'] = '''<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <gradient android:angle="315" android:startColor="#FF2A1450" android:centerColor="#FF1B1030" android:endColor="#FF0F0F17" android:type="linear" />
</shape>
'''
FL = 'M54,24 C60,34 72,42 72,58 C72,70 64,80 54,80 C44,80 36,70 36,58 C36,49 41,44 44,38 C45,45 48,48 51,49 C49,40 50,31 54,24 Z'
F['res/drawable/ic_launcher_fg.xml'] = '''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#FFFF5A2A" android:pathData="%s" />
    <path android:fillColor="#FFFF9A3C" android:pathData="M54,40 C58,47 66,52 66,62 C66,70 61,76 54,76 C47,76 42,70 42,62 C42,57 45,54 47,50 C48,55 50,57 52,58 C51,52 52,46 54,40 Z" />
    <path android:fillColor="#FFFFE07A" android:pathData="M54,56 C57,60 60,63 60,67 C60,71 57,74 54,74 C51,74 48,71 48,67 C48,64 50,62 51,60 C52,62 53,63 54,63 C53,61 53,58 54,56 Z" />
</vector>
''' % FL
F['res/drawable/ic_stat.xml'] = '''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#FFFFFFFF" android:pathData="M12,2 C13.5,4.6 17,6.8 17,11.2 C17,15.2 14.8,18 12,18 C9.2,18 7,15.2 7,11.8 C7,9.4 8.2,8 9,6.4 C9.3,8.2 10.1,9.2 11,9.5 C10.4,7 10.8,4.4 12,2 Z" />
</vector>
'''
F['res/drawable/widget_bg.xml'] = '''<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="#F21A1A26" />
    <corners android:radius="24dp" />
</shape>
'''
F['res/drawable/widget_btn.xml'] = '''<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="#FF263A5C" />
    <corners android:radius="14dp" />
</shape>
'''
F['res/drawable/widget_row.xml'] = '''<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="#FF242434" />
    <corners android:radius="14dp" />
</shape>
'''
F['res/layout/widget_streak.xml'] = '''<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/root" android:layout_width="match_parent" android:layout_height="match_parent"
    android:background="@drawable/widget_bg">
    <ImageView android:id="@+id/img" android:layout_width="match_parent" android:layout_height="match_parent"
        android:scaleType="fitCenter" android:padding="4dp" />
</FrameLayout>
'''
F['res/layout/widget_water.xml'] = '''<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/root" android:layout_width="match_parent" android:layout_height="match_parent"
    android:orientation="vertical" android:background="@drawable/widget_bg" android:padding="6dp">
    <ImageView android:id="@+id/img" android:layout_width="match_parent" android:layout_height="0dp"
        android:layout_weight="1" android:scaleType="fitCenter" />
    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal">
        <TextView android:id="@+id/btn1" android:layout_width="0dp" android:layout_weight="1" android:layout_height="34dp"
            android:gravity="center" android:textColor="#FFFFFFFF" android:textSize="13sp" android:textStyle="bold"
            android:background="@drawable/widget_btn" android:layout_marginEnd="4dp" />
        <TextView android:id="@+id/btn2" android:layout_width="0dp" android:layout_weight="1" android:layout_height="34dp"
            android:gravity="center" android:textColor="#FFFFFFFF" android:textSize="13sp" android:textStyle="bold"
            android:background="@drawable/widget_btn" />
    </LinearLayout>
</LinearLayout>
'''
rows = ''
for i in range(3):
    rows += '''
    <LinearLayout android:id="@+id/row%d" android:layout_width="match_parent" android:layout_height="46dp"
        android:orientation="horizontal" android:gravity="center_vertical" android:background="@drawable/widget_row"
        android:layout_marginTop="4dp" android:paddingStart="10dp" android:paddingEnd="4dp">
        <TextView android:id="@+id/txt%d" android:layout_width="0dp" android:layout_weight="1" android:layout_height="wrap_content"
            android:textColor="#FFF1F1F8" android:textSize="13sp" android:maxLines="1" android:ellipsize="end" />
        <TextView android:id="@+id/chk%d" android:layout_width="44dp" android:layout_height="match_parent"
            android:gravity="center" android:textSize="18sp" android:textColor="#FF3DDC84" />
    </LinearLayout>''' % (i, i, i)
F['res/layout/widget_today.xml'] = '''<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/root" android:layout_width="match_parent" android:layout_height="match_parent"
    android:orientation="vertical" android:background="@drawable/widget_bg" android:padding="10dp">
    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal"
        android:gravity="center_vertical">
        <TextView android:id="@+id/title" android:layout_width="0dp" android:layout_weight="1" android:layout_height="wrap_content"
            android:textColor="#FFF1F1F8" android:textSize="15sp" android:textStyle="bold" />
        <TextView android:id="@+id/streak" android:layout_width="wrap_content" android:layout_height="wrap_content"
            android:textColor="#FFFF9A3C" android:textSize="15sp" android:textStyle="bold" />
    </LinearLayout>
    <TextView android:id="@+id/sub" android:layout_width="match_parent" android:layout_height="wrap_content"
        android:textColor="#FF9A9AB2" android:textSize="12sp" />%s
</LinearLayout>
''' % rows


def info(layout, w, h, minw, minh, cw, ch):
    return '''<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="%ddp" android:minHeight="%ddp"
    android:targetCellWidth="%d" android:targetCellHeight="%d"
    android:minResizeWidth="%ddp" android:minResizeHeight="%ddp"
    android:updatePeriodMillis="1800000"
    android:initialLayout="@layout/%s"
    android:resizeMode="horizontal|vertical"
    android:widgetCategory="home_screen" />
''' % (w, h, cw, ch, minw, minh, layout)


F['res/xml/widget_streak_info.xml'] = info('widget_streak', 110, 110, 60, 60, 2, 2)
F['res/xml/widget_water_info.xml'] = info('widget_water', 110, 110, 90, 110, 2, 2)
F['res/xml/widget_today_info.xml'] = info('widget_today', 250, 180, 180, 150, 4, 3)
for k, v in F.items():
    os.makedirs(os.path.dirname(k), exist_ok=True)
    with open(k, 'w', encoding='utf-8') as f:
        f.write(v)
print('ok', len(F))
