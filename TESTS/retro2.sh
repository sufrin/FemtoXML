#
# Retrotesting
#

function femto()
{
  java -jar CLASS/femtoprint.jar "$@"
}

function ------()
{ echo "----------------------------------------------------------------"
}

function test()
{ 
  for m in "$@"
  do
      echo $m
  done
  femto /dev/stdin
  ------
}

function pretty()
{ for m in "$@"
  do
      echo $m
  done
  femto -i /dev/stdin
  ------
}

function litcontent()
{ echo -n "LIT CONTENT "
  for m in "$@"
  do
      echo $m
  done
  femto -x /dev/stdin
  ------
}

################################################################################

test 'DTD 1 (Entity declaration scanning)' <<END
<?xml encoding="UTF-8"?>
<!DOCTYPE test
 [
  <!ENTITY sys SYSTEM '1'>
  <!ENTITY % sys SYSTEM '2'>
  <!ENTITY   sys PUBLIC '3' '/3'>
  <!ENTITY   sys '4' '/4'>
  <!ENTITY % sys PUBLIC '5' '/5'>
  <!ENTITY % foo '6'>
  <!ENTITY foo '7'>
 ]>
END

test 'DTD 2 (Malformed system entity declaration)' <<END
<?xml encoding="UTF-8"?>
<!DOCTYPE test [<!ENTITY sys SYSTEM '1' 'a'>]>
END

test 'DTD 3A (Malformed public entity declaration)' <<END
<?xml encoding="UTF-8"?>
<!DOCTYPE test [<!ENTITY sys PUBLIC '1'>]>
END

test 'DTD 3B (Parameter entity substitution)' <<END
<?xml encoding="UTF-8"?>
<!DOCTYPE test SYSTEM 'xyzzy' 
  [<!ENTITY % p "PEE">
   <!ENTITY % q "CUL">
   <!ENTITY % pq "%p;%q;">
   <!ENTITY r "[[%pq;]]">
   ]>
   <show p="&p;" q="&q;" r="&r;"/>
END

test 'DTD 4A (From W3C Website: http://www.w3.org/TR/REC-xml/#dt-repltext)' <<END
<?xml encoding="UTF-8"?>
<!DOCTYPE test
[<!ENTITY % pub    "&#xc9;ditions Gallimard" >
 <!ENTITY   rights "All rights reserved" >
 <!ENTITY   book   "La Peste: Albert Camus,
&#xA9; 1947 %pub;. &rights;" >
]>
<book> &book; </book>
END

litcontent 'DTD 4B (expanding within entities)' << END
<?xml encoding="UTF-8"?>
<!DOCTYPE test
[
 <!ENTITY amp1 "&amp;amp;" >
]
>
<p>&amp1;</p>
END

litcontent 'DTD 4C (From W3C Website: http://www.w3.org/TR/REC-xml/#sec-entexpand)' <<END
<?xml encoding="UTF-8"?>
<!DOCTYPE test
[
 <!ENTITY example "<p>An ampersand (&#38;#38;) may be escaped
 numerically (&#38;#38;#38;) or with a general entity
 (&amp;amp;).</p>" >
]>
<book> &example; </book>
END


