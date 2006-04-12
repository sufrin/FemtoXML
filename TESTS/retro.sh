#
# Retrotesting
#
function test()
{ for m in "$@"
  do
      echo $m
  done
  java -jar CLASS/femtoprint.jar /dev/stdin
  echo ---------------------
}

test 'TEST 1'   <<END
<?xml version="1.0" charset="UTF8"?>
<!-- Should yield <tag arg="&arg;"/> -->
<tag arg="&arg;"/>
END

test 'TEST 2'   <<END
<?xml version="1.0" charset="UTF8"?>
<!DOCTYPE nonsense [<!ENTITY arg "arg">]>
<!-- Should yield <tag arg="arg"/> -->
<tag arg="&arg;"/>
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



