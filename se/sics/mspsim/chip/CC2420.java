/**
 * Copyright (c) 2007, 2008, 2009 Swedish Institute of Computer Science.
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
 * $Id$
 *
 * -----------------------------------------------------------------
 *
 * CC2420
 *
 * Author  : Joakim Eriksson
 * Created : Sun Oct 21 22:00:00 2007
 *
 */

package se.sics.mspsim.chip;

import se.sics.mspsim.core.*;
import se.sics.mspsim.util.Utils;

public class CC2420 extends Chip implements USARTListener, RFListener, RFSource {

  public enum Reg {
    SNOP, SXOSCON, STXCAL, SRXON, /* 0x00 */
    STXON, STXONCCA, SRFOFF, SXOSCOFF, /* 0x04 */
    SFLUSHRX, SFLUSHTX, SACK, SACKPEND, /* 0x08 */
    SRXDEC, STXENC, SAES, foo,   /* 0x0c */
    MAIN, MDMCTRL0, MDMCTRL1, RSSI, /* 0x10 */ 
    SYNCWORD, TXCTRL, RXCTRL0, RXCTRL1, /* 0x14 */
    FSCTRL, SECCTRL0, SECCTRL1, BATTMON, /* 0x18 */
    IOCFG0, IOCFG1, MANFIDL, MANFIDH, /* 0x1c */
    FSMTC, MANAND, MANOR, AGCCTRL, /* 0x20 */
    AGCTST0, AGCTST1, AGCTST2, FSTST0, /* 0x24 */
    FSTST1, FSTST2, FSTST3, RXBPFTST, /* 0x28 */
    FSMSTATE, ADCTST, DACTST, TOPTST,
    RESERVED, RES1, RES2, RES3,  /* 0x30 */
    RES4, RES5, RES6, RES7,
    RES8, RES9, RESa, RESb,
    RESc, RESd, TXFIFO, RXFIFO
  };

  public enum SpiState {
    WAITING, WRITE_REGISTER, READ_REGISTER, RAM_ACCESS,
    READ_RXFIFO, WRITE_TXFIFO
  };


  public static final int REG_SNOP		= 0x00;
  public static final int REG_SXOSCON	        = 0x01;
  public static final int REG_STXCAL		= 0x02;
  public static final int REG_SRXON		= 0x03;
  public static final int REG_STXON		= 0x04;
  public static final int REG_STXONCCA	        = 0x05;
  public static final int REG_SRFOFF		= 0x06;
  public static final int REG_SXOSCOFF	        = 0x07;
  public static final int REG_SFLUSHRX	        = 0x08;
  public static final int REG_SFLUSHTX	        = 0x09;
  public static final int REG_SACK		= 0x0A;
  public static final int REG_SACKPEND	        = 0x0B;
  public static final int REG_SRXDEC		= 0x0C;
  public static final int REG_STXENC		= 0x0D;
  public static final int REG_SAES		= 0x0E;
  public static final int REG_foo		= 0x0F;
  public static final int REG_MAIN		= 0x10;
  public static final int REG_MDMCTRL0	        = 0x11;
  public static final int REG_MDMCTRL1	        = 0x12;
  public static final int REG_RSSI		= 0x13;
  public static final int REG_SYNCWORD	        = 0x14;
  public static final int REG_TXCTRL		= 0x15;
  public static final int REG_RXCTRL0	        = 0x16;
  public static final int REG_RXCTRL1	        = 0x17;
  public static final int REG_FSCTRL		= 0x18;
  public static final int REG_SECCTRL0	        = 0x19;
  public static final int REG_SECCTRL1       	= 0x1A;
  public static final int REG_BATTMON   	= 0x1B;
  public static final int REG_IOCFG0		= 0x1C;
  public static final int REG_IOCFG1		= 0x1D;
  public static final int REG_MANFIDL   	= 0x1E;
  public static final int REG_MANFIDH   	= 0x1F;
  public static final int REG_FSMTC		= 0x20;
  public static final int REG_MANAND		= 0x21;
  public static final int REG_MANOR		= 0x22;
  public static final int REG_AGCCTRL    	= 0x23;
  public static final int REG_AGCTST0   	= 0x24;
  public static final int REG_AGCTST1   	= 0x25;
  public static final int REG_AGCTST2   	= 0x26;
  public static final int REG_FSTST0		= 0x27;
  public static final int REG_FSTST1		= 0x28;
  public static final int REG_FSTST2		= 0x29;
  public static final int REG_FSTST3		= 0x2A;
  public static final int REG_RXBPFTST    	= 0x2B;
  public static final int REG_FSMSTATE   	= 0x2C;
  public static final int REG_ADCTST		= 0x2D;
  public static final int REG_DACTST		= 0x2E;
  public static final int REG_TOPTST		= 0x2F;
  public static final int REG_RESERVED   	= 0x30;
  /* 0x31 - 0x3D not used */
  public static final int REG_TXFIFO		= 0x3E;
  public static final int REG_RXFIFO		= 0x3F;

  public static final int STATUS_XOSC16M_STABLE = 1 << 6;
  public static final int STATUS_TX_UNDERFLOW   = 1 << 5;
  public static final int STATUS_ENC_BUSY	    = 1 << 4;
  public static final int STATUS_TX_ACTIVE	= 1 << 3;
  public static final int STATUS_LOCK	= 1 << 2;
  public static final int STATUS_RSSI_VALID	= 1 << 1;

  // IOCFG0 Register Bit masks
  public static final int BCN_ACCEPT = (1<<11);
  public static final int FIFO_POLARITY = (1<<10);
  public static final int FIFOP_POLARITY = (1<<9);
  public static final int SFD_POLARITY = (1<<8);
  public static final int CCA_POLARITY = (1<<7);
  public static final int FIFOP_THR = 0x7F;

  // IOCFG1 Register Bit Masks
  public static final int SFDMUX = 0x3E0;
  public static final int CCAMUX = 0x1F;

  // CCAMUX values
  public static final int CCAMUX_CCA = 0;
  public static final int CCAMUX_XOSC16M_STABLE = 24;


  // RAM Addresses
  public static final int RAM_TXFIFO	= 0x000;
  public static final int RAM_RXFIFO	= 0x080;
  public static final int RAM_KEY0	= 0x100;
  public static final int RAM_RXNONCE	= 0x110;
  public static final int RAM_SABUF	= 0x120;
  public static final int RAM_KEY1	= 0x130;
  public static final int RAM_TXNONCE	= 0x140;
  public static final int RAM_CBCSTATE	= 0x150;
  public static final int RAM_IEEEADDR	= 0x160;
  public static final int RAM_PANID	= 0x168;
  public static final int RAM_SHORTADDR	= 0x16A;

  // The Operation modes of the CC2420
  public static final int MODE_TXRX_OFF = 0x00;
  public static final int MODE_RX_ON = 0x01;
  public static final int MODE_TXRX_ON = 0x02;
  public static final int MODE_POWER_OFF = 0x03;
  public static final int MODE_MAX = MODE_POWER_OFF;
  private static final String[] MODE_NAMES = new String[] {
    "off", "listen", "transmit", "power_off"
  };

  // State Machine - Datasheet Figure 25 page 44
  public enum RadioState {
     VREG_OFF, // -1;
     POWER_DOWN, // 0;
     IDLE, // 1;
     RX_CALIBRATE, // 2;
     RX_SFD_SEARCH, // 3;
     RX_WAIT, // 14;
     RX_FRAME, // 16;
     RX_OVERFLOW, // 17;
     TX_CALIBRATE, // 32;
     TX_PREAMBLE, // 34;
     TX_FRAME, // 37;
     TX_ACK_CALIBRATE, // 48;
     TX_ACK_PREABLE, // 49;
     TX_ACK, // 52;
     TX_UNDERFLOW// 56;
  };
  
  public static final int STATE_VREG_OFF = -1;
  public static final int STATE_POWER_DOWN = 0;
  public static final int STATE_IDLE = 1;
  public static final int STATE_RX_CALIBRATE = 2;
  public static final int STATE_RX_SFD_SEARCH = 3;
  public static final int STATE_RX_WAIT = 14;
  public static final int STATE_RX_FRAME = 16;
  public static final int STATE_RX_OVERFLOW = 17;
  public static final int STATE_TX_CALIBRATE = 32;
  public static final int STATE_TX_PREAMBLE = 34;
  public static final int STATE_TX_FRAME = 37;
  public static final int STATE_TX_ACK_CALIBRATE = 48;
  public static final int STATE_TX_ACK_PREABLE = 49;
  public static final int STATE_TX_ACK = 52;
  public static final int STATE_TX_UNDERFLOW = 56;

  // FCF High
  public static final int FRAME_TYPE = 0xC0;
  public static final int SECURITY_ENABLED = (1<<6);
  public static final int FRAME_PENDING = (1<<5);
  public static final int ACK_REQUEST = (1<<4);
  public static final int INTRA_PAN = (1<<3);
  // FCF Low
  public static final int DESTINATION_ADDRESS_MODE = 0x30;
  public static final int SOURCE_ADDRESS_MODE = 0x3;
  
  private RadioState stateMachine = RadioState.VREG_OFF;

  // 802.15.4 symbol period in ms
  public static final double SYMBOL_PERIOD = 0.016; // 16 us

  // when reading registers this flag is set!
  public static final int FLAG_READ = 0x40;

  public static final int FLAG_RAM = 0x80;
  // When accessing RAM the second byte of the address contains
  // a flag indicating read/write
  public static final int FLAG_RAM_READ = 0x20;

  private SpiState state = SpiState.WAITING;
  private int pos;
  private int address;
  private int shrPos;
  private int txfifoPos;
  private boolean txfifoFlush;	// TXFIFO is automatically flushed on next write
  private int rxfifoWritePos;
  private int rxfifoReadPos;
  private int rxfifoLen;
  private int rxlen;
  private int rxread;
  private int lastPacketStart;
  private int zeroSymbols;
  private boolean ramRead = false;

  /* RSSI is an externally set value of the RSSI for this CC2420 */
  /* low RSSI => CCA = true in normal mode */

  private int rssi = 0;
  private static int RSSI_OFFSET = -45; /* cc2420 datasheet */
  /* current CCA value */
  private boolean cca = false;
  
  private int activeFrequency = 0;
  private int activeChannel = 0;

  //private int status = STATUS_XOSC16M_STABLE | STATUS_RSSI_VALID;
  private int status = 0;

  private int[] registers = new int[64];
  // More than needed...
  private int[] memory = new int[512];

  // Buffer to hold 5 byte Synchronization header, as it is not written to the TXFIFO
  private byte[] SHR = new byte[5];

  private boolean chipSelect;

  private IOPort ccaPort = null;
  private int ccaPin;

  private IOPort fifopPort = null;
  private int fifopPin;
  /* fifoP state */
  private boolean fifoP = false;

  private IOPort fifoPort = null;
  private int fifoPin;

  private IOPort sfdPort = null;
  private int sfdPin;

  private int txCursor;
  private RFListener listener;
  private boolean on;

  private MSP430Core cpu;

  private TimeEvent oscillatorEvent = new TimeEvent(0) {
    public void execute(long t) {
      status |= STATUS_XOSC16M_STABLE;
      if(DEBUG) log("Oscillator Stable Event.");
      setState(RadioState.IDLE);
      if( (registers[REG_IOCFG1] & CCAMUX) == CCAMUX_XOSC16M_STABLE) {
        updateCCA();
      } else {
        if(DEBUG) log("CCAMUX != CCA_XOSC16M_STABLE! Not raising CCA");
      }
    }
  };

  private TimeEvent vregEvent = new TimeEvent(0) {
    public void execute(long t) {
      if(DEBUG) log("VREG Started at: " + t + " cyc: " +
          cpu.cycles + " " + getTime());
      on = true;
      setState(RadioState.POWER_DOWN);
      updateCCA();
    }
  };

  private TimeEvent sendEvent = new TimeEvent(0) {
    public void execute(long t) {
      txNext();
    }
  };

  private TimeEvent shrEvent = new TimeEvent(0) {
    public void execute(long t) {
      shrNext();
    }
  };

  private TimeEvent symbolEvent = new TimeEvent(0) {
    public void execute(long t) {
      switch(stateMachine) {
      case RX_CALIBRATE:
        setState(RadioState.RX_SFD_SEARCH);
        break;

      case TX_CALIBRATE:
        setState(RadioState.TX_PREAMBLE);
        break;

      case RX_WAIT:
        setState(RadioState.RX_SFD_SEARCH);
        break;
      }
    }
  };
  private boolean currentSFD;
  private boolean currentFIFO;
  private boolean overflow = false;

  public interface StateListener {
    public void newState(RadioState state);
  }

  private StateListener stateListener = null;

  public void setStateListener(StateListener listener) {
    stateListener = listener;
  }

  public RadioState getState() {
    return stateMachine;
  }

  // TODO: super(cpu) and chip autoregister chips into the CPU.
  public CC2420(MSP430Core cpu) {
    registers[REG_SNOP] = 0;
    registers[REG_TXCTRL] = 0xa0ff;
    this.cpu = cpu;
    setModeNames(MODE_NAMES);
    setMode(MODE_POWER_OFF);
    fifoP = false;
    rxfifoReadPos = 0;
    rxfifoWritePos = 0;
    overflow = false;
    cpu.addChip(this);
  }
  
  private boolean setState(RadioState state) {
    if(DEBUG) log("State transition from " + stateMachine + " to " + state);
    stateMachine = state;

    switch(stateMachine) {

    case VREG_OFF:
      if (DEBUG) log("VREG Off.");
      flushRX();
      flushTX();
      status &= ~(STATUS_RSSI_VALID | STATUS_XOSC16M_STABLE);
      setMode(MODE_POWER_OFF);
      updateCCA();
      break;

    case POWER_DOWN:
      rxfifoReadPos = 0;
      rxfifoWritePos = 0;
      status &= ~(STATUS_RSSI_VALID | STATUS_XOSC16M_STABLE);
      setMode(MODE_POWER_OFF);
      updateCCA();
      break;

    case RX_CALIBRATE:
      setSymbolEvent(12);
      setMode(MODE_RX_ON);
      break;

    case RX_SFD_SEARCH:
      zeroSymbols = 0;
      // RSSI valid here?
      status |= STATUS_RSSI_VALID;
      updateCCA();
      setMode(MODE_RX_ON);
      break;

    case TX_CALIBRATE:
      /* 12 symbols calibration, and one byte's wait since we deliver immediately
       * to listener when after calibration?
       */
      setSymbolEvent(12 + 2);
      setMode(MODE_TXRX_ON);
      break;

    case TX_PREAMBLE:
      shrPos = 0;
      SHR[0] = 0;
      SHR[1] = 0;
      SHR[2] = 0;
      SHR[3] = 0;
      SHR[4] = 0x7A;
      shrNext();
      break;

    case TX_FRAME:
      txfifoPos = 0;
      txNext();
      break;

    case RX_WAIT:
      setSymbolEvent(8);
      setMode(MODE_RX_ON);
      break;
      
    case IDLE:
      status &= ~STATUS_RSSI_VALID;
      setMode(MODE_TXRX_OFF);
      updateCCA();
      break;
    }

    /* Notify state listener */
    if (stateListener != null) {
      stateListener.newState(stateMachine);
    }

    return true;
  }

  /* Receive a byte from the radio medium
   * @see se.sics.mspsim.chip.RFListener#receivedByte(byte)
   */
  public void receivedByte(byte data) {
    // Received a byte from the "air"

    log("RF Byte received: " + Utils.hex8(data) + " state: " + stateMachine + " noZeroes: " + zeroSymbols +
        ((stateMachine == RadioState.RX_SFD_SEARCH || stateMachine == RadioState.RX_FRAME) ? "" : " *** Ignored"));

    if(stateMachine == RadioState.RX_SFD_SEARCH) {
      // Look for the preamble (4 zero bytes) followed by the SFD byte 0x7A
      if(data == 0) {
        // Count zero bytes
        zeroSymbols++;
      } else if(zeroSymbols >= 4 && data == 0x7A) {
        // If the received byte is !zero, we have counted 4 zero bytes prior to this one,
        // and the current received byte == 0x7A (SFD), we're in sync.
        // In RX mode, SFD goes high when the SFD is received
        setSFD(true);
        if (DEBUG) log("RX: Preamble/SFD Synchronized.");
        rxread = 0;
        setState(RadioState.RX_FRAME);
      } else {
        /* if not four zeros and 0x7A then no zeroes... */
        zeroSymbols = 0;
      }

    } else if(stateMachine == RadioState.RX_FRAME) {
      if(rxfifoLen == 128) {
        setRxOverflow();
      } else {		  
        memory[RAM_RXFIFO + rxfifoWritePos] = data & 0xFF;
        rxfifoWritePos = (rxfifoWritePos + 1) & 127;
        rxfifoLen++;

        if(rxread == 0) {
          rxlen = data & 0xff;
          if (DEBUG) log("RX: Start frame length " + rxlen);
          // FIFO pin goes high after length byte is written to RXFIFO
          setFIFO(true);
        }

        if(rxread++ == rxlen) {
          // In RX mode, FIFOP goes high, if threshold is higher than frame length....

          // Should take a RSSI value as input or use a set-RSSI value...
          memory[RAM_RXFIFO + ((rxfifoWritePos + 128 - 2) & 127)] = (registers[REG_RSSI]) & 0xff;
          // Set CRC ok and add a correlation
          memory[RAM_RXFIFO + ((rxfifoWritePos + 128 - 1) & 127)] = 37 | 0x80;
          setFIFOP(true);
          setSFD(false);
          lastPacketStart = (rxfifoWritePos + 128 - rxlen) & 127;
          if (DEBUG) log("RX: Complete: packetStart: " + 
              lastPacketStart);
          setState(RadioState.RX_WAIT);
        }
      }
    }
  }

  public void dataReceived(USART source, int data) {
    int oldStatus = status;
    if (DEBUG) {
      log("byte received: " + Utils.hex8(data) +
          " (" + ((data >= ' ' && data <= 'Z') ? (char) data : '.') + ')' +
          " CS: " + chipSelect + " SPI state: " + state + " StateMachine: " + stateMachine);
    }

    if ((stateMachine != RadioState.VREG_OFF) && chipSelect) {

      switch(state) {
      case WAITING:
        if ((data & FLAG_READ) != 0) {
          state = SpiState.READ_REGISTER;
        } else {
          state = SpiState.WRITE_REGISTER;
        }
        if ((data & FLAG_RAM) != 0) {
          state = SpiState.RAM_ACCESS;
          address = data & 0x7f;
        } else {
          // The register address
          address = data & 0x3f;

          if (address == REG_RXFIFO) {
            // check read/write???
            //          log("Reading RXFIFO!!!");
            state = SpiState.READ_RXFIFO;
          } else if (address == REG_TXFIFO) {
            state = SpiState.WRITE_TXFIFO;
          }
        }
        if (data < 0x0f) {
          strobe(data);
          state = SpiState.WAITING;
        }
        pos = 0;
        // Assuming that the status always is sent back???
        //source.byteReceived(status);
        break;
      case WRITE_REGISTER:
        if (pos == 0) {
          source.byteReceived(registers[address] >> 8);
          // set the high bits
          registers[address] = (registers[address] & 0xff) | (data << 8);
          pos = 1; 
        } else {
          source.byteReceived(registers[address] & 0xff);
          // set the low bits
          registers[address] = (registers[address] & 0xff00) | data;
          if (address == REG_IOCFG0) {
            setFIFOP(false);
          }

          if (DEBUG) {
            log("wrote to " + Utils.hex8(address) + " = "
                + registers[address]);
            switch(address) {
            case REG_IOCFG0:
            	log("IOCFG0: " + registers[address]);
            	break;
            case REG_IOCFG1:
            	log("IOCFG1: SFDMUX "
            			+ ((registers[address] & SFDMUX) >> SFDMUX)
            			+ " CCAMUX: " + (registers[address] & CCAMUX));
//            	if( (registers[address] & CCAMUX) == CCA_CCA)
//            	  setCCA(false);
            	updateCCA();
            	break;
            }
          }
          /* register written - go back to wating... */
          state = SpiState.WAITING;
        }
        break;
      case READ_REGISTER:
        if (pos == 0) {
          source.byteReceived(registers[address] >> 8);
          pos = 1;
        } else {
          source.byteReceived(registers[address] & 0xff);
          if (DEBUG) {
            log("read from " + Utils.hex8(address) + " = "
                + registers[address]);
          }
          state = SpiState.WAITING;
        }
        return;
        //break;
      case READ_RXFIFO:
        if(DEBUG) log("RXFIFO READ " + rxfifoReadPos + " => " +
            (memory[RAM_RXFIFO + rxfifoReadPos] & 0xFF) + " size: " + rxfifoLen);
        source.byteReceived( (memory[RAM_RXFIFO + rxfifoReadPos] & 0xFF) );

        rxfifoReadPos = (rxfifoReadPos + 1) & 127;
        
        if (rxfifoLen > 0) {
          rxfifoLen--;
        }
        // Set the FIFO pin low if there are no more bytes available in the RXFIFO.
        if(rxfifoLen == 0) {
          if (DEBUG) log("Setting FIFO to low (buffer empty)");
          setFIFO(false);
        }
        
        // TODO:
        // -MT FIFOP is lowered when there are less than IOCFG0:FIFOP_THR bytes in the RXFIFO
        // If FIFO_THR is greater than the frame length, FIFOP goes low when the first byte is read out.
        // As long as we are in "OVERFLOW" the fifoP is not cleared.
        if (fifoP && !overflow) {
          if (DEBUG) log("*** FIFOP cleared at: " + rxfifoReadPos +
              " lastPacketStartPos: " + lastPacketStart);
          setFIFOP(false);
        }
        return;
      case WRITE_TXFIFO:
        if(txfifoFlush) {
          txCursor = 0;
          txfifoFlush = false;
        }
        if (DEBUG) log("Writing data: " + data + " to tx: " + txCursor);

        memory[RAM_TXFIFO + txCursor++] = data & 0xff;
        if (sendEvents) {
          sendEvent("WRITE_TXFIFO", null);
        }
        break;
      case RAM_ACCESS:
        if (pos == 0) {
          address |= (data << 1) & 0x180;
          ramRead = (data & 0x20) != 0;
          if (DEBUG) {
            log("Address: " + Utils.hex16(address) +
                " read: " + ramRead);
          }
          pos++;
        } else {
          if (!ramRead) {
            memory[address++] = data;
            if (DEBUG && address == RAM_PANID + 2) {
              log("Pan ID set to: 0x" +
                  Utils.hex8(memory[RAM_PANID]) +
                  Utils.hex8(memory[RAM_PANID + 1]));
            }
          } else {
            //log("Read RAM Addr: " + address + " Data: " + memory[address]);  
            source.byteReceived(memory[address++]);
            return;
          }
        }
        break;
      }
      source.byteReceived(oldStatus);  
    }
  }

  // Needs to get information about when it is possible to write
  // next data...
  private void strobe(int data) {
    // Resets, on/off of different things...
    if (DEBUG) {
      log("Strobe on: " + Utils.hex8(data) + " => " + Reg.values()[data]);
    }

    if( (stateMachine == RadioState.POWER_DOWN) && (data != REG_SXOSCON) ) {
      if (DEBUG) log("Got command strobe: " + data + " in POWER_DOWN.  Ignoring.");
      return;
    }

    switch (data) {
    case REG_SNOP:
      if (DEBUG) log("SNOP => " + Utils.hex8(status) + " at " + cpu.cycles);
      break;
    case REG_SRXON:
      if(stateMachine == RadioState.IDLE) {
        setState(RadioState.RX_CALIBRATE);
        //updateActiveFrequency();
        if (DEBUG) {
            log("Strobe RX-ON!!!");
        }
      }else{
        if (DEBUG) log("WARNING: SRXON when not IDLE");
      }

      break;
    case REG_SRFOFF:
      if (DEBUG) {
        log("Strobe RXTX-OFF!!! at " + cpu.cycles);
      }
      setState(RadioState.IDLE);
      break;
    case REG_STXON:
      // State transition valid from IDLE state or all RX states
      if( (stateMachine == RadioState.IDLE) || 
          (stateMachine == RadioState.RX_CALIBRATE) ||
          (stateMachine == RadioState.RX_SFD_SEARCH) ||
          (stateMachine == RadioState.RX_FRAME) ||
          (stateMachine == RadioState.RX_OVERFLOW) ||
          (stateMachine == RadioState.RX_WAIT)) {
        status |= STATUS_TX_ACTIVE;
        setState(RadioState.TX_CALIBRATE);
        if (sendEvents) {
          sendEvent("STXON", null);
        }
        // Starting up TX subsystem - indicate that we are in TX mode!
        if (DEBUG) log("Strobe STXON - transmit on! at " + cpu.cycles);
      }
      break;
    case REG_STXONCCA:
      // Only valid from all RX states,
      // since CCA requires ??(look this up) receive symbol periods to be valid
      if( (stateMachine == RadioState.RX_CALIBRATE) ||
          (stateMachine == RadioState.RX_SFD_SEARCH) ||
          (stateMachine == RadioState.RX_FRAME) ||
          (stateMachine == RadioState.RX_OVERFLOW) ||
          (stateMachine == RadioState.RX_WAIT)) {
        
        if (sendEvents) {
          sendEvent("STXON_CCA", null);
        }
        
        if(cca) {
          status |= STATUS_TX_ACTIVE;
          setState(RadioState.TX_CALIBRATE);
          if (DEBUG) log("Strobe STXONCCA - transmit on! at " + cpu.cycles);
        }else{
          if (DEBUG) log("STXONCCA Ignored, CCA false");
        }
      }
      break;
    case REG_SFLUSHRX:
      flushRX();
      break;
    case REG_SFLUSHTX:
      if (DEBUG) log("Flushing TXFIFO");
      flushTX();
      break;
    case REG_SXOSCON:
      //log("Strobe Oscillator On");
      startOscillator();
      break;
    case REG_SXOSCOFF:
      //log("Strobe Oscillator Off");
      stopOscillator();
      break;
    default:
      if (DEBUG) {
        log("Unknown strobe command: " + data);
      }
    break;
    }
  }

  private void shrNext() {
    if(shrPos == 5) {
      // Set SFD high
      setSFD(true);
      setState(RadioState.TX_FRAME);
    } else {
      if (listener != null) {
        if (DEBUG) log("transmitting byte: " + Utils.hex8(SHR[shrPos]));
        listener.receivedByte(SHR[shrPos]);
      }
      shrPos++;
      cpu.scheduleTimeEventMillis(shrEvent, SYMBOL_PERIOD * 2);
    }
  }

  private void txNext() {
    if(txfifoPos <= memory[RAM_TXFIFO]) {
      if (txfifoPos > 0x7f) {
        log("Warning: packet size too large - repeating packet bytes txfifoPos: " + txfifoPos);
      }
      if (listener != null) {
        if (DEBUG) log("transmitting byte: " + Utils.hex8(memory[RAM_TXFIFO + (txfifoPos & 0x7f)] & 0xFF));
        listener.receivedByte((byte)(memory[RAM_TXFIFO + (txfifoPos & 0x7f)] & 0xFF));
      }
      txfifoPos++;
      // Two symbol periods to send a byte...
      long time = cpu.scheduleTimeEventMillis(sendEvent, SYMBOL_PERIOD * 2);
//      log("Scheduling 2 SYMB at: " + time + " getTime(now): " + cpu.getTime());
    } else {
      if (DEBUG) log("Completed Transmission.");
      status &= ~STATUS_TX_ACTIVE;
      setSFD(false);
      if (overflow) {
        /* TODO: is it going back to overflow here ?=? */
        setState(RadioState.RX_OVERFLOW);
      } else {
        setState(RadioState.RX_CALIBRATE);
      }
      /* Back to RX ON */
      setMode(MODE_RX_ON);
      txfifoFlush = true;
    }
  }

  private void setSymbolEvent(int symbols) {
    double period = SYMBOL_PERIOD * symbols;
    cpu.scheduleTimeEventMillis(symbolEvent, period);
    //log("Set Symbol event: " + period);
  }

  private void startOscillator() {
    // 1ms crystal startup from datasheet pg12
    cpu.scheduleTimeEventMillis(oscillatorEvent, 1);
  }

  private void stopOscillator() {
    status &= ~STATUS_XOSC16M_STABLE;
    setState(RadioState.POWER_DOWN);
    if (DEBUG) log("Oscillator Off.");
    // Reset state
    setFIFOP(false);
  }

  private void flushRX() {
    if (DEBUG) {
      log("Flushing RX len = " + rxfifoLen);
    }
    rxfifoReadPos = 0;
    rxfifoWritePos = 0;
    rxfifoLen = 0;
    setSFD(false);
    setFIFOP(false);
    setFIFO(false);
    overflow = false;
    /* goto RX Calibrate */
    if( (stateMachine == RadioState.RX_CALIBRATE) ||
        (stateMachine == RadioState.RX_SFD_SEARCH) ||
        (stateMachine == RadioState.RX_FRAME) ||
        (stateMachine == RadioState.RX_OVERFLOW) ||
        (stateMachine == RadioState.RX_WAIT)) {
      setState(RadioState.RX_SFD_SEARCH);
    }
  }

  // TODO: update any pins here?
  private void flushTX() {
    txCursor = 0;
  }
  
  private void updateCCA() {
    boolean oldCCA = cca;
    int ccaMux = (registers[REG_IOCFG1] & CCAMUX);

    if (ccaMux == CCAMUX_CCA) {
      /* If RSSI is less than -95 then we have CCA / clear channel! */
      cca = (status & STATUS_RSSI_VALID) > 0 && rssi < -95;
    } else if (ccaMux == CCAMUX_XOSC16M_STABLE) {
      cca = (status & STATUS_XOSC16M_STABLE) > 0;
    }
    
    if (cca != oldCCA) {
      setInternalCCA(cca);
    }
  }

  private void setInternalCCA(boolean clear) {
    setCCAPin(clear);
    if (DEBUG) log("Internal CCA: " + clear);
  }

  
  private void setSFD(boolean sfd) {
    if( (registers[REG_IOCFG0] & SFD_POLARITY) == SFD_POLARITY)
      sfdPort.setPinState(sfdPin, sfd ? 0 : 1);
    else 
      sfdPort.setPinState(sfdPin, sfd ? 1 : 0);
    currentSFD = sfd;
    if (DEBUG) log("SFD: " + sfd + "  " + cpu.cycles);
  }

  private void setCCAPin(boolean cca) {
    if (DEBUG) log("Setting CCA to: " + cca);
    if( (registers[REG_IOCFG0] & CCA_POLARITY) == CCA_POLARITY)
      ccaPort.setPinState(ccaPin, cca ? 0 : 1);
    else
      ccaPort.setPinState(ccaPin, cca ? 1 : 0);
  }

  private void setFIFOP(boolean fifop) {
    fifoP = fifop;
    if (DEBUG) log(getName() + " setting FIFOP to " + fifop);
    if( (registers[REG_IOCFG0] & FIFOP_POLARITY) == FIFOP_POLARITY) {
      fifopPort.setPinState(fifopPin, fifop ? 0 : 1);
    } else {
      fifopPort.setPinState(fifopPin, fifop ? 1 : 0);
    }
  }

  private void setFIFO(boolean fifo) {
    if (DEBUG) log(getName() + " setting FIFO to " + fifo);
    currentFIFO = fifo;
    fifoPort.setPinState(fifoPin, fifo ? 1 : 0);
  }

  
  private void setRxOverflow() {
    if (DEBUG) log("RXFIFO Overflow! Read Pos: " + rxfifoReadPos + " Write Pos: " + rxfifoWritePos);
    setFIFOP(true);
    setFIFO(false);
    setSFD(false);
    overflow = true;
    setState(RadioState.RX_OVERFLOW);
  }
  
  
  /*****************************************************************************
   *  External APIs for simulators simulating Radio medium, etc.
   * 
   *****************************************************************************/
  public void updateActiveFrequency() {
    /* INVERTED: f = 5 * (c - 11) + 357 + 0x4000 */
    activeFrequency = registers[REG_FSCTRL] - 357 + 2405 - 0x4000;
    activeChannel = (registers[REG_FSCTRL] - 357 - 0x4000)/5 + 11;
  }

  public int getActiveFrequency() {
    return activeFrequency;
  }

  public int getActiveChannel() {
    return activeChannel;
  }

  public int getOutputPowerIndicator() {
    return (registers[REG_TXCTRL] & 0x1f);
  }

  public void setRSSI(int power) {
    if (DEBUG) log("external setRSSI to: " + power);
    if (power < -128) {
      power = -128;
    }
    rssi = power;
    registers[REG_RSSI] = power - RSSI_OFFSET;
    updateCCA();
  }

  public int getRSSI() {
    return rssi;
  }

  public int getOutputPower() {
    /* From CC2420 datasheet */
    int indicator = getOutputPowerIndicator();
    if (indicator >= 31) {
      return 0;
    } else if (indicator >= 27) {
      return -1;
    } else if (indicator >= 23) {
      return -3;
    } else if (indicator >= 19) {
      return -5;
    } else if (indicator >= 15) {
      return -7;
    } else if (indicator >= 11) {
      return -10;
    } else if (indicator >= 7) {
      return -15;
    } else if (indicator >= 3) {
      return -25;
    }

    /* Unknown */
    return -100;
  }


  public void setRFListener(RFListener rf) {
    listener = rf;
  }

  public void setVRegOn(boolean newOn) {
    if(on == newOn) return;

    if(newOn) {
      // 0.6ms maximum vreg startup from datasheet pg 13
      cpu.scheduleTimeEventMillis(vregEvent, 0.1);
      if (DEBUG) log(getName() + ": Scheduling vregEvent at: cyc = " + cpu.cycles +
         " target: " + vregEvent.getTime() + " current: " + cpu.getTime());
    } else {
      on = false;
      setState(RadioState.VREG_OFF);
    }
  }

  public void setChipSelect(boolean select) {
    chipSelect = select;
    if (!chipSelect) {
      state = SpiState.WAITING;
    }

    if (DEBUG) {
      log("setting chipSelect: " + chipSelect);
    }
  }

  public boolean getChipSelect() {
    return chipSelect;
  }
  
  public void setCCAPort(IOPort port, int pin) {
    ccaPort = port;
    ccaPin = pin;
  }

  public void setFIFOPPort(IOPort port, int pin) {
    fifopPort = port;
    fifopPin = pin;
  }

  public void setFIFOPort(IOPort port, int pin) {
    fifoPort = port;
    fifoPin = pin;
  }

  public void setSFDPort(IOPort port, int pin) {
    sfdPort = port;
    sfdPin = pin;
  }


  // -------------------------------------------------------------------
  // Methods for accessing and writing to registers, etc from outside
  // And for receiveing data
  // -------------------------------------------------------------------

  public int getRegister(int register) {
    return registers[register];
  }

  public void setRegister(int register, int data) {
    registers[register] = data;
  }

  /* External API for radio mediums - will change to setRSSI only later...*/
  public void setCCA(boolean clear) {
    if (DEBUG) log("*** CCA set to: " + clear + " ignored....");
//    if (clear) {
//      rssi = 0;
//    } else { 
//      rssi = 127;
//    }
//    updateCCA();
  }

  /*****************************************************************************
   * Chip APIs
   *****************************************************************************/
  public String getName() {
    return "CC2420";
  }

  public int getModeMax() {
    return MODE_MAX;
  }
  
  public String chipinfo() {
    updateActiveFrequency();
    return " VREG_ON: " + on + " ChipSel: " + chipSelect +
    "\n OSC_Stable: " + ((status & STATUS_XOSC16M_STABLE) > 0) + 
    "\n RSSI_Valid: " + ((status & STATUS_RSSI_VALID) > 0) + "  CCA: " + cca +
    "\n FIFOP Polarity: " + ((registers[REG_IOCFG0] & FIFOP_POLARITY) == FIFOP_POLARITY) +
    " FIFOP: " + fifoP + " FIFO: " + currentFIFO + " SFD: " + currentSFD + 
    "\n Radio State: " + stateMachine + " rxFifoLen: " + rxfifoLen + " rxFifoWritePos: " +
      rxfifoWritePos + " rxFifoReadPos: " + rxfifoReadPos +
    "\n SPI State: " + state +
    "\n Channel: " + activeChannel +
    "\n";
  }

  public void stateChanged(int state) {
  }
  
} // CC2420
