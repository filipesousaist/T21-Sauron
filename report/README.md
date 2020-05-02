# Relatório do projeto Sauron

Sistemas Distribuídos 2019-2020, segundo semestre


## Autores

**Grupo T21**

| Number | Name           | User                                | Email                                           |
| -------|----------------|-------------------------------------| ------------------------------------------------|
| 90714  | Filipe Sousa   | <https://github.com/filipesousaist> | <mailto:filipe.miguel.sousa@tecnico.ulisboa.pt> |
| 90762  | Pedro Vilela   | <https://github.com/pedro19v>       | <mailto:pedro.vilela@tecnico.ulisboa.pt>        |
| 90766  | Pedro Pereira  | <https://github.com/pedro99p>       | <mailto:pedro.l.pereira@tecnico.ulisboa.pt>     |

![Filipe Sousa](90714.png) ![Pedro Vilela](90762.png) ![Pedro Pereira](90766.png)

## Modelo de faltas

_(que faltas são toleradas, que faltas não são toleradas)_


## Solução

_(Figura da solução de tolerância a faltas)_

_(Breve explicação da solução, suportada pela figura anterior)_


## Protocolo de replicação

In order to fulfill the requirement of having a high availability and internet partition tolerant system we used a
a variant of the gossip protocol. The gossip protocol in the project's scenario is used to share information about 
observations and cameras across all server replicas. One of the changes we made to the original protocol was only 
having one timestamp per replica. The original protocol has two timestamps: the value timestamp, which represents
the state of the update log, and the value timestamp, which represents the state of the replica. One of the
objectives of having an update log is to keep track of updated that cannot be executed due to causal dependencies.
In our case there are no causal dependencies between observations and between cameras, therefore the 
update log and the value timestamp will always remain consistent, which is why only one timestamp is needed. 

How it works?

Periodically, a replica will request all other replicas to send it the observations and cameras they have and
provides them with its actual timestamp. The other replicas will send the updates which they know the replica does
not have, by looking at the timestamp the replica sent them. The replica will then update its update log, the value
and its timestamp with the incoming updates. 

This way, we guarantee that the replicas, most of the time will have the most recent data, thus providing high
availability.

Furthermore, if a replica for some reason goes down(it needed to perform a reset, or the power fails), when it
comes back up, it will quickly be updated with the most recent information. This happens because when the replica
comes back up its timestamp will be at zeros. Then when it asks the other replicas for updates the other replicas 
will realize that that replica has a very low timestamp and will send it a big chunk of updates and, this way, in
one round of gossip messages the replica that just came back up will have the most recent updates. 



## Opções de implementação

_(Descrição de opções de implementação, incluindo otimizações e melhorias introduzidas)_



## Notas finais

_(Algo mais a dizer?)_
