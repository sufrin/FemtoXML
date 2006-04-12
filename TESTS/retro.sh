#
# Retrotesting
#
TESTS = "$@"

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

################################################################################

test 'TEST 1'   <<END
<!-- Should yield <tag arg="&arg;"/> -->
<tag arg="&arg;"/>
END

test 'TEST 2'   <<END
<!DOCTYPE test [<!ENTITY arg "arg">]>
<tag arg="&arg;&amp;">xx&amp;yy</tag>
END

test 'TEST 2A'   <<END
<!DOCTYPE test [<!ENTITY arg "arg">]>
<tag arg="&arg;&amp;">xx&#x3d;yy</tag>
END

test 'TEST 3'   <<END
<doc>
        <section>
          <para/>
        </section>
        <section>
          <para/>
        </section>
        <section>
          <para/>
        </section>
</doc>
END

test 'TEST 4 (improperly nested)'   <<END
<doc>
        <section>
          <para/>
        </section>
        <section>
          <para/>
        </section>
        <section>
          <para/>
</doc>
END

test 'TEST 5 (superfluous closing tag in entity)'   <<END
<!DOCTYPE test [<!ENTITY closesection '</section>'>]>
<doc>
        <section>
          <para/>
        </section>
        <section>
          <para/>
        </section>
        <section>
          <para/>
        &closesection;
</doc>
END

test 'TEST 6 (entity not well-formed)'   <<END
<!DOCTYPE test [<!ENTITY opensection '<section>'>]>
<doc>
        &opensection;
          <para/>
        </section>
        <section>
          <para/>
        </section>
        <section>
          <para/>
        </section>
</doc>
END

test 'TEST 7 (error in nested entity)'   <<END
<!DOCTYPE test 
    [<!ENTITY inner '<section></donkey>'>
     <!ENTITY outer '<outer>&inner;</outer>'>
    ]>
<doc>
        <section>
          &outer;
        </section>
</doc>
END

test 'TEST 8 (well-formed nested entity; space perservation)'   <<END
<!DOCTYPE test 
 [ <!ENTITY section   '<section>nested entity</section>'>
   <!ENTITY verse     '<verse xml:space="preserve"> 
        It&apos;s a long way to Tiperrary 
        It&apos;s a long way to go!</verse>'>]>
<doc>
        &section;
        <section>
          &verse;
        </section>
        <section>
          This is a long piece of text that should get
          wrapped around with any luck. I am not sure
          whether you think it is a good idea.  This is a
          long piece of text that should get wrapped around
          with any luck. I am not sure whether you think
          it is a good idea.  This is a long piece of text
          that should get wrapped around with any luck. I
          am not sure whether you think it is a good idea.
        </section>
        <section xml:space="preserve">
          This is a long piece of text that should get
          wrapped around with any luck. I am not sure
          whether you think it is a good idea.  This is a
          long piece of text that should get wrapped around
          with any luck. I am not sure whether you think
          it is a good idea.  This is a long piece of text
          that should get wrapped around with any luck. I
          am not sure whether you think it is a good idea.</section>
</doc>
END

pretty 'TEST 8A (prettyprint TEST 8)'   <<END
<!DOCTYPE test 
 [ <!ENTITY section   '<section>nested entity</section>'>
   <!ENTITY verse     '<verse xml:space="preserve"> 
        It&apos;s a long way to Tiperrary 
        It&apos;s a long way to go!</verse>'>]>
<doc>
        &section;
        <section>
          &verse;
        </section>
        <section>
          This is a long piece of text that should get
          wrapped around with any luck. I am not sure
          whether you think it is a good idea.  This is a
          long piece of text that should get wrapped around
          with any luck. I am not sure whether you think
          it is a good idea.  This is a long piece of text
          that should get wrapped around with any luck. I
          am not sure whether you think it is a good idea.
        </section>
        <section xml:space="preserve">
          This is a long piece of text that should get
          wrapped around with any luck. I am not sure
          whether you think it is a good idea.  This is a
          long piece of text that should get wrapped around
          with any luck. I am not sure whether you think
          it is a good idea.  This is a long piece of text
          that should get wrapped around with any luck. I
          am not sure whether you think it is a good idea.</section>
</doc>
END

test 'TEST 9 (entity in bad position)'<<END
<test foo &eq; "bar"/>
END

test 'TEST 10 (runaway entity in bad position)'<<END
<test foo &eq "bar"/>
END

test 'TEST 11 (entity in bad position)'<<END
<test &foo; = "bar"/>
END

test 'TEST 12 (entity in bad position)'<<END
<!DOCTYPE test [<!ENTITY test 'name'>]>
<&test; foo = "bar"/>
END

test 'TEST 13 (runaway entity name)'<<END
<test foo &eq "bar"/>
END

test 'TEST 14 (runaway element tag)'<<END
<test foo="bar"
END

test 'TEST 15 (runaway string)'<<END
<test foo="bar
END

echo 'TEST 16 (prettyprint fixpoint of a large file)'
femto -i TESTS/bigtest.xml >/tmp/a
femto -i /tmp/a            >/tmp/b
diff /tmp/a /tmp/b
------

echo 'TEST 17 (fixpoint of a large file)'
femto  TESTS/bigtest.xml >/tmp/a
femto  /tmp/a            >/tmp/b
diff /tmp/a /tmp/b
------

echo 'TEST 18 (Suppressing comments, etc.)'
femto -c -d -p /dev/stdin<<END
<?xml version="1.0" ?>
<!DOCTYPE  ignore 
 [ <!ENTITY section   '<section>nested entity</section>'>
   <!ENTITY verse     '<verse xml:space="preserve"> 
        It&apos;s a long way to Tiperrary 
        It&apos;s a long way to go!</verse>'>
 ]>
<start>
 <!-- comment -->
 <? PI ?>
</start>
END
------

echo 'TEST 19 (Suppressing all but doctype)'
femto -c -p /dev/stdin<<END
<?xml version="1.0"?>
<!DOCTYPE  ignore 
 [ <!ENTITY section   '<section>nested entity</section>'>
   <!ENTITY verse     '<verse xml:space="preserve"> 
        It&apos;s a long way to Tiperrary 
        It&apos;s a long way to go!</verse>'>]>
<start>
 <!-- comment -->
 <? PI ?>
</start>
END
------

echo 'TEST 20 (Suppressing doctype)'
femto -d /dev/stdin<<END
<?xml version="1.0"?>
<!DOCTYPE  ignore 
 [ <!ENTITY section   '<section>nested entity</section>'>
   <!ENTITY verse     '<verse xml:space="preserve"> 
        It&apos;s a long way to Tiperrary 
        It&apos;s a long way to go!</verse>'>]>
<start>
 <!-- comment -->
 <? PI ?>
</start>
END
------

