/**
 * Copyright (c) 2011, Swedish Institute of Computer Science.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * This file is part of MSPSim.
 *
 *
 * -----------------------------------------------------------------
 *
 * Author  : Joakim Eriksson
 */

package se.sics.mspsim.config;

import java.util.ArrayList;

import se.sics.mspsim.core.ADC12;
import se.sics.mspsim.core.DMA;
import se.sics.mspsim.core.IOPort;
import se.sics.mspsim.core.IOUnit;
import se.sics.mspsim.core.InterruptMultiplexer;
import se.sics.mspsim.core.MSP430Config;
import se.sics.mspsim.core.MSP430Core;
import se.sics.mspsim.core.Multiplier;
import se.sics.mspsim.core.Timer;
import se.sics.mspsim.core.USART;

public class MSP430f1611Config extends MSP430Config {

    
    public MSP430f1611Config() {
        maxInterruptVector = 15;

        /* configuration for the timers */
        TimerConfig timerA = new TimerConfig(6, 5, 3, 0x160, Timer.TIMER_Ax149, "TimerA", Timer.TAIV);
        TimerConfig timerB = new TimerConfig(13, 12, 7, 0x180, Timer.TIMER_Bx149, "TimerB", Timer.TBIV);
 
        timerConfig = new TimerConfig[] {timerA, timerB};
        
        /* configure memory */
        infoMemConfig(0x1000, 128 * 2);
        mainFlashConfig(0x4000, 48 * 1024);
        ramConfig(0x1100, 10 * 1024);
    }
    

    public int setup(MSP430Core cpu, ArrayList<IOUnit> ioUnits) {
        System.out.println("*** Setting up f1611 IO!");

        USART usart0 = new USART(cpu, 0, cpu.memory, 0x70);
        USART usart1 = new USART(cpu, 1, cpu.memory, 0x78);
        
        for (int i = 0, n = 8; i < n; i++) {
          cpu.memOut[0x70 + i] = usart0;
          cpu.memIn[0x70 + i] = usart0;

          cpu.memOut[0x78 + i] = usart1;
          cpu.memIn[0x78 + i] = usart1;
        }
        
        Multiplier mp = new Multiplier(cpu, cpu.memory, 0);
        // Only cares of writes!
        for (int i = 0x130, n = 0x13f; i < n; i++) {
          cpu.memOut[i] = mp;
          cpu.memIn[i] = mp;
        }
        
        // Usarts
        ioUnits.add(usart0);
        ioUnits.add(usart1);

        DMA dma = new DMA("dma", cpu.memory, 0, cpu);
        for (int i = 0, n = 24; i < n; i++) {    
            cpu.memOut[0x1E0 + i] = dma;
            cpu.memIn[0x1E0 + i] = dma;
        }

        /* DMA Ctl */
        cpu.memOut[0x122] = dma;
        cpu.memIn[0x124] = dma;
        
        /* configure the DMA */
        dma.setDMATrigger(DMA.URXIFG0, usart0, 0);
        dma.setDMATrigger(DMA.UTXIFG0, usart0, 1);
        dma.setDMATrigger(DMA.URXIFG1, usart1, 0);
        dma.setDMATrigger(DMA.UTXIFG1, usart1, 1);
        dma.setInterruptMultiplexer(new InterruptMultiplexer(cpu, 0));

        ioUnits.add(dma);
        
        // Add port 1,2 with interrupt capability!
        IOPort io1;
        IOPort io2;
        ioUnits.add(io1 = new IOPort(cpu, 1, 4, cpu.memory, 0x20));
        ioUnits.add(io2 = new IOPort(cpu, 2, 1, cpu.memory, 0x28));
        for (int i = 0, n = 8; i < n; i++) {
          cpu.memOut[0x20 + i] = io1;
          cpu.memOut[0x28 + i] = io2;
          cpu.memIn[0x20 + i] = io1;
          cpu.memIn[0x28 + i] = io2;
        }

        // Add port 3,4 & 5,6
        for (int i = 0, n = 2; i < n; i++) {
          IOPort p = new IOPort(cpu, (3 + i), 0, cpu.memory, 0x18 + i * 4);
          ioUnits.add(p);
          cpu.memOut[0x18 + i * 4] = p;
          cpu.memOut[0x19 + i * 4] = p;
          cpu.memOut[0x1a + i * 4] = p;
          cpu.memOut[0x1b + i * 4] = p;
          cpu.memIn[0x18 + i * 4] = p;
          cpu.memIn[0x19 + i * 4] = p;
          cpu.memIn[0x1a + i * 4] = p;
          cpu.memIn[0x1b + i * 4] = p;
        }

        for (int i = 0, n = 2; i < n; i++) {
          IOPort p = new IOPort(cpu, (5 + i), 0, cpu.memory, 0x30 + i * 4);
          ioUnits.add(p);
          cpu.memOut[0x30 + i * 4] = p;
          cpu.memOut[0x31 + i * 4] = p;
          cpu.memOut[0x32 + i * 4] = p;
          cpu.memOut[0x33 + i * 4] = p;
          cpu.memIn[0x30 + i * 4] = p;
          cpu.memIn[0x31 + i * 4] = p;
          cpu.memIn[0x32 + i * 4] = p;
          cpu.memIn[0x33 + i * 4] = p;
        }
        
        ADC12 adc12 = new ADC12(cpu);
        ioUnits.add(adc12);

        for (int i = 0, n = 16; i < n; i++) {
            cpu.memOut[0x80 + i] = adc12;
            cpu.memIn[0x80 + i] = adc12;
            cpu.memOut[0x140 + i] = adc12;
            cpu.memIn[0x140 + i] = adc12;
            cpu.memOut[0x150 + i] = adc12;
            cpu.memIn[0x150 + i] = adc12;
        }
        for (int i = 0, n = 8; i < n; i++) {    
            cpu.memOut[0x1A0 + i] = adc12;
            cpu.memIn[0x1A0 + i] = adc12;
        }
        
        return 3 + 6;
    }
    
    
    
}
