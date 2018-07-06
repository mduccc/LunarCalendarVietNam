<h1>Lunar Calendar VietNam</h1>
<p>Base: https://www.informatik.uni-leipzig.de/~duc/amlich/calrules.html</p>
<h3>Language: Kotlin</h3>

<h4>Use:</h4>
<pre>/*dd: Int (day), mm: Int (month), yy: Int (year), 7f: float (timezone of VietNam)*/</pre>

<code>LunarCalendar().convertSolar2Lunar(dd, mm, yy, 7f)</code>


<h4>Result:</h4>
<pre>
{
  'lunarDay': '{value}',
  'lunarMonth': {value}}',
  'lunarYear': '{value}'
}
</pre>
